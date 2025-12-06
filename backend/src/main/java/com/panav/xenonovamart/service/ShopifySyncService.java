package com.panav.xenonovamart.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panav.xenonovamart.model.StoreEntity;
import com.panav.xenonovamart.repository.StoreRepository;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ShopifySyncService {

    private final StoreRepository storeRepo;
    private final ShopifyWebhookService webhookService;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShopifySyncService(StoreRepository storeRepo,
                              ShopifyWebhookService webhookService) {
        this.storeRepo = storeRepo;
        this.webhookService = webhookService;
        this.webClient = WebClient.create();
    }

    public void syncStore(Long storeId) throws Exception {
        StoreEntity store = storeRepo.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        String shopDomain = store.getShopDomain();
        String accessToken = store.getAccessToken();
        if (accessToken == null) {
            throw new IllegalStateException("Store has no access token yet: " + shopDomain);
        }

        // Try this:
        syncOrders(shopDomain, accessToken);    // already wrapped with 403 catch
        syncCustomers(shopDomain, accessToken); // ❌ comment this out for now
        syncProducts(shopDomain, accessToken);  // still useful

        store.setLastFullSyncAt(OffsetDateTime.now());
        storeRepo.save(store);
    }



    private void syncOrders(String shopDomain, String accessToken) throws Exception {
        String url = "https://" + shopDomain + "/admin/api/2025-07/orders.json?status=any&limit=50";

        try {
            String response = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-Shopify-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode orders = root.path("orders");
            if (orders.isArray()) {
                for (JsonNode orderNode : orders) {
                    String orderJson = orderNode.toString();
                    webhookService.handleOrderCreate(shopDomain, orderJson);
                }
            }
        } catch (WebClientResponseException.Forbidden e) {
            System.out.println("⚠ Shopify 403 on orders sync for shop " + shopDomain +
                    ". Likely missing read_orders / protected data permissions. Skipping orders sync.");
            System.out.println("Response body: " + e.getResponseBodyAsString());
            // do NOT rethrow
        } catch (WebClientResponseException e) {
            System.out.println("⚠ Shopify error on orders sync: " + e.getStatusCode());
            System.out.println("Body: " + e.getResponseBodyAsString());
            // optionally skip or rethrow - for now just skip
        }
    }

    private void syncCustomers(String shopDomain, String accessToken) throws Exception {
        String url = "https://" + shopDomain + "/admin/api/2025-07/customers.json?limit=50";

        try {
            String response = webClient.get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-Shopify-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode customers = root.path("customers");
            if (customers.isArray()) {
                for (JsonNode node : customers) {
                    String json = node.toString();
                    webhookService.handleCustomerCreate(shopDomain, json);
                }
            }
        } catch (WebClientResponseException.Forbidden e) {
            System.out.println("⚠ Shopify 403 on customers sync for shop " + shopDomain +
                    ". Protected customer data. Skipping customers sync via REST.");
            System.out.println("Response body: " + e.getResponseBodyAsString());
            // don't rethrow
        } catch (WebClientResponseException e) {
            System.out.println("⚠ Shopify error on customers sync: " + e.getStatusCode());
            System.out.println("Body: " + e.getResponseBodyAsString());
            // choose to skip or rethrow; for assignment, skip
        }
    }


    private void syncProducts(String shopDomain, String accessToken) throws Exception {
        String url = "https://" + shopDomain + "/admin/api/2025-07/products.json?limit=50";

        String response = webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Shopify-Access-Token", accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode root = objectMapper.readTree(response);
        JsonNode products = root.path("products");
        if (products.isArray()) {
            for (JsonNode node : products) {
                String json = node.toString();
                webhookService.handleProductCreate(shopDomain, json);
            }
        }
    }

    // Optional: sync all stores (for scheduled job)
    public void syncAllStores() {
        List<StoreEntity> stores = storeRepo.findAll();
        for (StoreEntity store : stores) {
            try {
                syncStore(store.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
