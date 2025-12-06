package com.panav.xenonovamart.controller;

import com.panav.xenonovamart.model.AppUserEntity;
import com.panav.xenonovamart.model.StoreEntity;
import com.panav.xenonovamart.repository.AppUserRepository;
import com.panav.xenonovamart.repository.StoreRepository;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserRepository userRepo;
    private final StoreRepository storeRepo;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(AppUserRepository userRepo, StoreRepository storeRepo) {
        this.userRepo = userRepo;
        this.storeRepo = storeRepo;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        AppUserEntity user = userRepo.findByEmail(req.getEmail())
                .orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        Long orgId = user.getOrganization().getId();
        List<StoreEntity> stores = storeRepo.findByOrganizationId(orgId);

        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getId());
        resp.setOrgId(orgId);
        resp.setEmail(user.getEmail());
        resp.setStores(stores);

        // In production: generate JWT here.
        resp.setToken("dummy-token-" + user.getId());

        return ResponseEntity.ok(resp);
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class LoginResponse {
        private Long userId;
        private Long orgId;
        private String email;
        private String token;      // simple placeholder token
        private List<StoreEntity> stores;
    }
}
