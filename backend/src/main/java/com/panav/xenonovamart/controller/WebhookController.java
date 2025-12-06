package com.panav.xenonovamart.controller;

import com.panav.xenonovamart.service.ShopifyWebhookService;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;


@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @Value("${app.shopify.webhookSecret}")
    private String webhookSecret;

    private final ShopifyWebhookService webhookService;

    public WebhookController(ShopifyWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/orders_create")
    public ResponseEntity<String> ordersCreate(
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmac,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestBody byte[] rawBody) {

        try {
            // Test webhook sent from Shopify admin dashboard
            if (hmac == null) {
                System.out.println("⚠ TEST ORDERS WEBHOOK:");
                System.out.println(new String(rawBody, StandardCharsets.UTF_8));
                return ResponseEntity.ok("OK (test)");
            }

            // Validate HMAC using raw bytes + webhook secret
            if (!isValidHmac(rawBody, hmac, webhookSecret)) {
                System.out.println("❌ INVALID HMAC for orders/create");
                System.out.println("BODY:");
                System.out.println(new String(rawBody, StandardCharsets.UTF_8));
                return ResponseEntity.status(401).body("Invalid HMAC");
            }

            String body = new String(rawBody, StandardCharsets.UTF_8);

            System.out.println("✔ REAL ORDER WEBHOOK RECEIVED:");
            System.out.println(body);

            webhookService.handleOrderCreate(shopDomain, body);
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("error: " + e.getMessage());
        }
    }


    @PostMapping("/customers_create")
    public ResponseEntity<String> customersCreate(
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmac,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestBody byte[] rawBody) {

        try {
            if (hmac == null) {
                System.out.println("TEST CUSTOMER WEBHOOK:");
                System.out.println(new String(rawBody, StandardCharsets.UTF_8));
                return ResponseEntity.ok("OK (test)");
            }

            // Validate HMAC using raw bytes
            if (!isValidHmac(rawBody, hmac, webhookSecret)) {
                System.out.println("❌ INVALID HMAC");
                System.out.println("BODY:");
                System.out.println(new String(rawBody, StandardCharsets.UTF_8));
                return ResponseEntity.status(401).body("Invalid HMAC");
            }

            String body = new String(rawBody, StandardCharsets.UTF_8);

            System.out.println("✔ REAL CUSTOMER WEBHOOK RECEIVED:");
            System.out.println(body);

            webhookService.handleCustomerCreate(shopDomain, body);
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("error: " + e.getMessage());
        }
    }

    @PostMapping("/products_create")
    public ResponseEntity<String> productsCreate(
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmac,
            @RequestHeader(value = "X-Shopify-Shop-Domain", required = false) String shopDomain,
            @RequestBody byte[] rawBody) {

        try {
            if (hmac == null) {
                System.out.println("⚠ TEST PRODUCT WEBHOOK:");
                System.out.println(new String(rawBody, StandardCharsets.UTF_8));
                return ResponseEntity.ok("OK (test)");
            }

            if (!isValidHmac(rawBody, hmac, webhookSecret)) {
                System.out.println("❌ INVALID HMAC for products/create");
                System.out.println("BODY:");
                System.out.println(new String(rawBody, StandardCharsets.UTF_8));
                return ResponseEntity.status(401).body("Invalid HMAC");
            }

            String body = new String(rawBody, StandardCharsets.UTF_8);

            System.out.println("✔ REAL PRODUCT WEBHOOK RECEIVED:");
            System.out.println(body);

            webhookService.handleProductCreate(shopDomain, body);
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("error: " + e.getMessage());
        }
    }






    private boolean isValidHmac(byte[] body, String hmacHeader, String secret) throws Exception {
        byte[] shopifyHmac = Base64.decodeBase64(hmacHeader);

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

        byte[] calculated = mac.doFinal(body);
        return MessageDigest.isEqual(calculated, shopifyHmac);
    }

}
