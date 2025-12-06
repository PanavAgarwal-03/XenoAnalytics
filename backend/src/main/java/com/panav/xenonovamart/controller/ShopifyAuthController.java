package com.panav.xenonovamart.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.panav.xenonovamart.model.AppUserEntity;
import com.panav.xenonovamart.model.OrganizationEntity;
import com.panav.xenonovamart.model.StoreEntity;
import com.panav.xenonovamart.repository.AppUserRepository;
import com.panav.xenonovamart.repository.OrganizationRepository;
import com.panav.xenonovamart.repository.StoreRepository;
import com.panav.xenonovamart.service.ShopifyWebhookRegistrar;
import com.panav.xenonovamart.util.PasswordGenerator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class ShopifyAuthController {

    @Value("${app.shopify.apiKey}")
    private String apiKey;

    @Value("${app.shopify.apiSecret}")
    private String apiSecret;

    @Value("${app.shopify.redirectUri}")
    private String redirectUri;

    private final WebClient webClient = WebClient.create();
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final ShopifyWebhookRegistrar webhookRegistrar;
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShopifyAuthController(OrganizationRepository organizationRepository,
                                 StoreRepository storeRepository,
                                 ShopifyWebhookRegistrar webhookRegistrar,
                                 AppUserRepository appUserRepository) {
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.webhookRegistrar = webhookRegistrar;
        this.appUserRepository = appUserRepository;
    }

    // Install redirect
    @GetMapping("/install")
    public String install(@RequestParam String shop) {
        String scopes = "read_products,read_customers,read_orders,write_products,write_orders";
        String state = "panav-demo-state-1"; // TODO: generate & persist a random state in real app
        String url = String.format(
                "https://%s/admin/oauth/authorize?client_id=%s&scope=%s&redirect_uri=%s&state=%s",
                URLEncoder.encode(shop, StandardCharsets.UTF_8),
                URLEncoder.encode(apiKey, StandardCharsets.UTF_8),
                URLEncoder.encode(scopes, StandardCharsets.UTF_8),
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8),
                URLEncoder.encode(state, StandardCharsets.UTF_8)
        );
        return "Open this URL in browser: " + url;
    }

    // OAuth callback (Shopify redirects here)
    @GetMapping("/auth/callback")
    public String callback(@RequestParam String code,
                           @RequestParam String shop,
                           @RequestParam String hmac,
                           @RequestParam String state) {

        // 1) Exchange code for access token
        String tokenUrl = "https://" + shop + "/admin/oauth/access_token";

        Map<String, String> body = Map.of(
                "client_id", apiKey,
                "client_secret", apiSecret,
                "code", code
        );

        Map resp = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (resp == null || resp.get("access_token") == null) {
            return "Failed to get access token: " + resp;
        }

        String accessToken = resp.get("access_token").toString();
        System.out.println("ACCESS_TOKEN=" + accessToken);

        try {
            // 2) Call /shop.json to get shop email + name
            String shopInfoJson = webClient.get()
                    .uri("https://" + shop + "/admin/api/2025-07/shop.json")
                    .header("X-Shopify-Access-Token", accessToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode shopRoot = objectMapper.readTree(shopInfoJson);
            JsonNode shopNode = shopRoot.path("shop");

            String shopName = shopNode.path("name").asText(shop);
            String shopEmail = shopNode.path("email").asText();

            System.out.println("Shop name: " + shopName);
            System.out.println("Shop email: " + shopEmail);

            // 3) Find or create Organization by adminEmail (shop owner email)
            OrganizationEntity org = organizationRepository
                    .findByAdminEmail(shopEmail)
                    .orElseGet(() -> {
                        OrganizationEntity o = OrganizationEntity.builder()
                                .name(shopName)
                                .adminEmail(shopEmail)
                                .build();
                        return organizationRepository.save(o);
                    });

            // 4) Find or create Store for this shop domain
            StoreEntity store = storeRepository
                    .findByShopDomain(shop)
                    .orElseGet(() -> StoreEntity.builder()
                            .shopDomain(shop)
                            .organization(org)
                            .build());

            store.setAccessToken(accessToken);
            storeRepository.save(store);

            // 5) Find or create AppUser for this org using shop email
            AppUserEntity user = appUserRepository.findByEmail(shopEmail)
                    .orElseGet(() -> {
                        String randomPassword = PasswordGenerator.generate(12);
                        String passwordHash = passwordEncoder.encode(randomPassword);

                        AppUserEntity u = AppUserEntity.builder()
                                .email(shopEmail)
                                .passwordHash(passwordHash)
                                .organization(org)
                                .build();
                        AppUserEntity saved = appUserRepository.save(u);

                        System.out.println("====== NEW ORG USER CREATED ======");
                        System.out.println("Login email: " + shopEmail);
                        System.out.println("Temporary password: " + randomPassword);
                        System.out.println("==================================");

                        return saved;
                    });

            // 6) Register webhooks for this store (products/create, etc.)
            webhookRegistrar.registerDefaultWebhooks(shop, accessToken);

            return "App installed for " + shop +
                    ". Organization: " + org.getName() +
                    ". Login email: " + shopEmail +
                    ". Check backend logs for the temporary password.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error during shop setup: " + e.getMessage();
        }
    }
}
