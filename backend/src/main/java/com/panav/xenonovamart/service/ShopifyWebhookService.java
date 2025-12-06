package com.panav.xenonovamart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panav.xenonovamart.model.*;
import com.panav.xenonovamart.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Iterator;

@Service
public class ShopifyWebhookService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final LineItemRepository lineItemRepo;
    private final StoreRepository storeRepo; // to map shop domain -> storeId
    private final ProductRepository productRepo;

    public ShopifyWebhookService(OrderRepository orderRepo,
                                 CustomerRepository customerRepo,
                                 LineItemRepository lineItemRepo,
                                 StoreRepository storeRepo,
                                 ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.lineItemRepo = lineItemRepo;
        this.storeRepo = storeRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public void handleOrderCreate(String shopDomain, String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        long shopifyOrderId = root.path("id").asLong();
        long orderNumber = root.path("order_number").asLong();
        String totalPriceStr = root.path("total_price").asText("0.00");
        String currency = root.path("currency").asText();
        String createdAt = root.path("created_at").asText();

        // get store
        var storeOpt = storeRepo.findByShopDomain(shopDomain);
        if (storeOpt.isEmpty()) throw new IllegalStateException("Store not found: " + shopDomain);
        var store = storeOpt.get();
        Long storeId = store.getId();
        Long orgId = store.getOrganization().getId();

        // idempotency
        if (orderRepo.findByStoreIdAndShopifyOrderId(storeId, shopifyOrderId).isPresent()) {
            return;
        }

        // customer part
        JsonNode cust = root.path("customer");
        Long shopifyCustomerId;
        if (!cust.isMissingNode() && !cust.isNull()) {
            shopifyCustomerId = cust.path("id").asLong();

            if (shopifyCustomerId != 0) {
                var cOpt = customerRepo.findByStoreIdAndShopifyCustomerId(storeId, shopifyCustomerId);
                CustomerEntity customer = cOpt.orElseGet(() -> CustomerEntity.builder()
                        .storeId(storeId)
                        .orgId(orgId)
                        .shopifyCustomerId(shopifyCustomerId)
                        .build());
                customer.setEmail(cust.path("email").asText(null));
                customer.setFirstName(cust.path("first_name").asText(null));
                customer.setLastName(cust.path("last_name").asText(null));
                customer.setDefaultAddressJson(cust.path("default_address").toString());
                customerRepo.save(customer);
            }
        } else {
            shopifyCustomerId = null;
        }

        // create order
        OrderEntity order = OrderEntity.builder()
                .storeId(storeId)
                .orgId(orgId)
                .shopifyOrderId(shopifyOrderId)
                .shopifyCustomerId(shopifyCustomerId)
                .orderNumber((int) orderNumber)
                .totalPrice(new BigDecimal(totalPriceStr))
                .currency(currency)
                .createdAt(OffsetDateTime.parse(createdAt))
                .rawPayload(body)
                .build();
        order = orderRepo.save(order);

        // 6) Line items
        JsonNode items = root.path("line_items");
        if (items.isArray()) {
            for (JsonNode li : items) {
                LineItemEntity line = LineItemEntity.builder()
                        .orderId(order.getId())
                        .shopifyLineItemId(li.path("id").asLong())
                        .productId(li.has("product_id") ? li.get("product_id").asLong() : null)
                        .variantId(li.has("variant_id") ? li.get("variant_id").asLong() : null)
                        .title(li.path("title").asText(null))
                        .quantity(li.path("quantity").asInt(0))
                        .price(new BigDecimal(li.path("price").asText("0.00")))
                        .sku(li.path("sku").asText(null))
                        .build();

                lineItemRepo.save(line);
            }
        }
        store.setLastOrderWebhookAt(OffsetDateTime.now());
        storeRepo.save(store);

        System.out.println("✔️ Order saved for store: " + shopDomain);
    }

    @Transactional
    public void handleCustomerCreate(String shopDomain, String body) throws Exception {

        // 1) Test webhook: No shop domain
        if (shopDomain == null) {
            System.out.println("⚠️ Skipping DB save: TEST customer webhook (no shop domain)");
            return;
        }

        JsonNode root = objectMapper.readTree(body);

        long shopifyCustomerId = root.path("id").asLong(0);
        if (shopifyCustomerId == 0) {
            System.out.println("⚠️ No customer ID in webhook — skipping");
            return;
        }

        // 2) Get store
        var storeOpt = storeRepo.findByShopDomain(shopDomain);
        if (storeOpt.isEmpty()) {
            System.out.println("❌ Store not found: " + shopDomain);
            return;
        }

        var store = storeOpt.get();
        Long storeId = store.getId();
        Long orgId = store.getOrganization().getId();

        // 3) Check if customer already exists
        var cOpt = customerRepo.findByStoreIdAndShopifyCustomerId(storeId, shopifyCustomerId);

        CustomerEntity customer = cOpt.orElseGet(() -> CustomerEntity.builder()
                .storeId(storeId)
                .orgId(orgId)
                .shopifyCustomerId(shopifyCustomerId)
                .build()
        );

        // 4) Update fields (safe for missing nodes)
        customer.setEmail(root.path("email").asText(null));
        customer.setFirstName(root.path("first_name").asText(null));
        customer.setLastName(root.path("last_name").asText(null));

        // Default address may NOT exist for new customers
        if (root.has("default_address") && !root.path("default_address").isMissingNode()) {
            customer.setDefaultAddressJson(root.path("default_address").toString());
        }

        // 5) Save
        customerRepo.save(customer);

        System.out.println("✔️ Customer saved: " + shopifyCustomerId + " for store: " + shopDomain);
    }

    @Transactional
    public void handleProductCreate(String shopDomain, String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);

        long shopifyProductId = root.path("id").asLong();
        String title = root.path("title").asText(null);
        String status = root.path("status").asText(null);
        String productType = root.path("product_type").asText(null);
        String vendor = root.path("vendor").asText(null);

        String createdAtStr = root.path("created_at").asText(null);
        String updatedAtStr = root.path("updated_at").asText(null);

        var storeOpt = storeRepo.findByShopDomain(shopDomain);
        if (storeOpt.isEmpty()) throw new IllegalStateException("Store not found: " + shopDomain);
        var store = storeOpt.get();
        Long storeId = store.getId();
        Long orgId = store.getOrganization().getId();

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;
        int totalVariants = 0;

        JsonNode variants = root.path("variants");
        if (variants.isArray()) {
            totalVariants = variants.size();
            for (JsonNode v : variants) {
                BigDecimal price = new BigDecimal(v.path("price").asText("0.00"));
                if (minPrice == null || price.compareTo(minPrice) < 0) minPrice = price;
                if (maxPrice == null || price.compareTo(maxPrice) > 0) maxPrice = price;
            }
        }

        var existingOpt = productRepo.findByStoreIdAndShopifyProductId(storeId, shopifyProductId);
        ProductEntity product = existingOpt.orElseGet(() -> ProductEntity.builder()
                .storeId(storeId)
                .orgId(orgId)
                .shopifyProductId(shopifyProductId)
                .build());

        product.setTitle(title);
        product.setStatus(status);
        product.setProductType(productType);
        product.setVendor(vendor);
        product.setMinPrice(minPrice);
        product.setMaxPrice(maxPrice);
        product.setTotalVariants(totalVariants);
        product.setRawPayload(body);

        if (createdAtStr != null && !createdAtStr.isBlank()) {
            product.setCreatedAt(OffsetDateTime.parse(createdAtStr));
        }
        if (updatedAtStr != null && !updatedAtStr.isBlank()) {
            product.setUpdatedAt(OffsetDateTime.parse(updatedAtStr));
        }

        productRepo.save(product);
    }


}
