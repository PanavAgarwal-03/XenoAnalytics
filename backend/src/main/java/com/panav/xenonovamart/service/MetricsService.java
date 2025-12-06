package com.panav.xenonovamart.service;

import com.panav.xenonovamart.dto.*;
import com.panav.xenonovamart.model.*;
import com.panav.xenonovamart.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MetricsService {

    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final LineItemRepository lineItemRepo;
    private final StoreRepository storeRepo;

    public MetricsService(OrderRepository orderRepo,
                          CustomerRepository customerRepo,
                          ProductRepository productRepo,
                          LineItemRepository lineItemRepo,
                          StoreRepository storeRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.lineItemRepo = lineItemRepo;
        this.storeRepo = storeRepo;
    }

    public MetricsSummaryDto getSummary(Long storeId, OffsetDateTime from, OffsetDateTime to) {
        // all orders in range
        List<OrderEntity> rangeOrders =
                orderRepo.findByStoreIdAndCreatedAtBetween(storeId, from, to);

        long totalOrders = rangeOrders.size();

        BigDecimal totalRevenue = rangeOrders.stream()
                .map(OrderEntity::getTotalPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            averageOrderValue = totalRevenue
                    .divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        long totalCustomers = customerRepo.countByStoreId(storeId);

        // New vs returning customers (simple definition)
        // - new: first order date in [from, to]
        // - returning: customers with >= 2 orders (overall)
        List<OrderEntity> allOrders = orderRepo.findByStoreId(storeId);

        Map<Long, List<OrderEntity>> ordersByCustomer = allOrders.stream()
                .filter(o -> o.getShopifyCustomerId() != null)
                .collect(Collectors.groupingBy(OrderEntity::getShopifyCustomerId));

        long newCustomers = ordersByCustomer.entrySet().stream()
                .filter(e -> {
                    List<OrderEntity> orders = e.getValue();
                    OffsetDateTime first = orders.stream()
                            .map(OrderEntity::getCreatedAt)
                            .min(OffsetDateTime::compareTo)
                            .orElse(null);
                    return first != null && !first.isBefore(from) && !first.isAfter(to);
                }).count();

        long returningCustomers = ordersByCustomer.values().stream()
                .filter(list -> list.size() >= 2)
                .count();

        return MetricsSummaryDto.builder()
                .totalCustomers(totalCustomers)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .averageOrderValue(averageOrderValue)
                .newCustomers(newCustomers)
                .returningCustomers(returningCustomers)
                .build();
    }

    public List<OrdersByDateDto> getOrdersByDate(Long storeId, LocalDate from, LocalDate to) {
        OffsetDateTime fromDt = from.atStartOfDay().atOffset(OffsetDateTime.now().getOffset());
        OffsetDateTime toDt = to.plusDays(1).atStartOfDay().atOffset(OffsetDateTime.now().getOffset());

        List<OrderEntity> rangeOrders =
                orderRepo.findByStoreIdAndCreatedAtBetween(storeId, fromDt, toDt);

        Map<LocalDate, List<OrderEntity>> byDate = rangeOrders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    LocalDate date = e.getKey();
                    List<OrderEntity> orders = e.getValue();
                    long count = orders.size();
                    BigDecimal total = orders.stream()
                            .map(OrderEntity::getTotalPrice)
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return OrdersByDateDto.builder()
                            .date(date)
                            .orderCount(count)
                            .totalRevenue(total)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<TopCustomerDto> getTopCustomers(Long storeId, OffsetDateTime from, OffsetDateTime to, int limit) {
        List<OrderEntity> rangeOrders =
                orderRepo.findByStoreIdAndCreatedAtBetween(storeId, from, to);

        // group by shopifyCustomerId
        Map<Long, List<OrderEntity>> byCustomer = rangeOrders.stream()
                .filter(o -> o.getShopifyCustomerId() != null)
                .collect(Collectors.groupingBy(OrderEntity::getShopifyCustomerId));

        List<TopCustomerDto> list = new ArrayList<>();

        for (Map.Entry<Long, List<OrderEntity>> entry : byCustomer.entrySet()) {
            Long shopifyCustomerId = entry.getKey();
            List<OrderEntity> orders = entry.getValue();

            BigDecimal totalSpend = orders.stream()
                    .map(OrderEntity::getTotalPrice)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long orderCount = orders.size();

            // enrich with customer data
            String email = null;
            String name = null;
            var custOpt = customerRepo.findByStoreIdAndShopifyCustomerId(storeId, shopifyCustomerId);
            if (custOpt.isPresent()) {
                var c = custOpt.get();
                email = c.getEmail();
                String first = c.getFirstName() == null ? "" : c.getFirstName();
                String last = c.getLastName() == null ? "" : c.getLastName();
                name = (first + " " + last).trim();
            }

            list.add(TopCustomerDto.builder()
                    .customerId(shopifyCustomerId)
                    .email(email)
                    .name(name)
                    .totalSpend(totalSpend)
                    .orderCount(orderCount)
                    .build());
        }

        return list.stream()
                .sorted(Comparator.comparing(TopCustomerDto::getTotalSpend).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<TopProductDto> getTopProducts(Long storeId, OffsetDateTime from, OffsetDateTime to, int limit) {
        List<OrderEntity> rangeOrders =
                orderRepo.findByStoreIdAndCreatedAtBetween(storeId, from, to);

        if (rangeOrders.isEmpty()) return List.of();

        List<Long> orderIds = rangeOrders.stream()
                .map(OrderEntity::getId)
                .collect(Collectors.toList());

        List<LineItemEntity> items = lineItemRepo.findByOrderIdIn(orderIds);

        // group by productId (Shopify product id)
        Map<Long, List<LineItemEntity>> byProduct = items.stream()
                .filter(li -> li.getProductId() != null)
                .collect(Collectors.groupingBy(LineItemEntity::getProductId));

        List<TopProductDto> list = new ArrayList<>();

        for (Map.Entry<Long, List<LineItemEntity>> entry : byProduct.entrySet()) {
            Long shopifyProductId = entry.getKey();
            List<LineItemEntity> lis = entry.getValue();

            long qty = lis.stream()
                    .map(LineItemEntity::getQuantity)
                    .mapToLong(Integer::longValue)
                    .sum();

            BigDecimal revenue = lis.stream()
                    .map(li -> li.getPrice().multiply(BigDecimal.valueOf(li.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String title = null;
            var productOpt = productRepo.findByStoreIdAndShopifyProductId(storeId, shopifyProductId);
            if (productOpt.isPresent()) {
                title = productOpt.get().getTitle();
            }

            list.add(TopProductDto.builder()
                    .productId(shopifyProductId)
                    .title(title)
                    .totalRevenue(revenue)
                    .quantitySold(qty)
                    .build());
        }

        return list.stream()
                .sorted(Comparator.comparing(TopProductDto::getTotalRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public SyncStatusDto getSyncStatus(Long storeId) {
        long customerCount = customerRepo.countByStoreId(storeId);
        long orderCount = orderRepo.countByStoreId(storeId);
        long productCount = productRepo.countByStoreId(storeId);

        StoreEntity store = storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        return SyncStatusDto.builder()
                .totalCustomers(customerCount)
                .totalOrders(orderCount)
                .totalProducts(productCount)
                .lastOrderWebhookAt(store.getLastOrderWebhookAt())
                .lastFullSyncAt(store.getLastFullSyncAt())
                .build();
    }
}
