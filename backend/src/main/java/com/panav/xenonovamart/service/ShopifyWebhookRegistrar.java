package com.panav.xenonovamart.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class ShopifyWebhookRegistrar {

    @Value("${app.publicBaseUrl}")
    private String publicBaseUrl;

    private final WebClient webClient = WebClient.create();

    public void registerDefaultWebhooks(String shopDomain, String accessToken) {
        // We will register 3 webhooks: orders/create, customers/create, products/create
        createWebhook(shopDomain, accessToken,
                "orders/create",
                publicBaseUrl + "/webhooks/orders_create");

        createWebhook(shopDomain, accessToken,
                "customers/create",
                publicBaseUrl + "/webhooks/customers_create");

        createWebhook(shopDomain, accessToken,
                "products/create",
                publicBaseUrl + "/webhooks/products_create");
    }

    private void createWebhook(String shopDomain, String accessToken, String topic, String address) {
        String url = "https://" + shopDomain + "/admin/api/2025-07/webhooks.json";

        Map<String, Object> body = Map.of(
                "webhook", Map.of(
                        "topic", topic,
                        "address", address,
                        "format", "json"
                )
        );

        try {
            Map resp = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Shopify-Access-Token", accessToken)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("Webhook created for topic " + topic + ": " + resp);
        } catch (Exception e) {
            System.err.println("Failed to create webhook for topic " + topic + ": " + e.getMessage());
        }
    }
}
