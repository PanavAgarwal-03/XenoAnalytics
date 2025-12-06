package com.panav.xenonovamart.controller;

import com.panav.xenonovamart.model.OrderEntity;
import com.panav.xenonovamart.repository.OrderRepository;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderQueryController {

    private final OrderRepository orderRepo;

    public OrderQueryController(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @GetMapping("/list")
    public List<OrderSummaryDto> listOrders(
            @RequestParam Long storeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // simple version: get all orders for store and filter in memory
        List<OrderEntity> all = orderRepo.findByStoreIdOrderByCreatedAtDesc(storeId);

        return all.stream()
                .filter(o -> {
                    if (from == null && to == null) return true;
                    LocalDate d = o.getCreatedAt().toLocalDate();
                    if (from != null && d.isBefore(from)) return false;
                    if (to != null && d.isAfter(to)) return false;
                    return true;
                })
                .map(o -> {
                    OrderSummaryDto dto = new OrderSummaryDto();
                    dto.setId(o.getId());
                    dto.setOrderNumber(o.getOrderNumber());
                    dto.setShopifyOrderId(o.getShopifyOrderId());
                    dto.setTotalPrice(o.getTotalPrice());
                    dto.setCurrency(o.getCurrency());
                    dto.setCreatedAt(o.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Data
    public static class OrderSummaryDto {
        private Long id;
        private Integer orderNumber;
        private Long shopifyOrderId;
        private java.math.BigDecimal totalPrice;
        private String currency;
        private OffsetDateTime createdAt;
    }
}
