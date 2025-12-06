package com.panav.xenonovamart.controller;

import com.panav.xenonovamart.service.ShopifySyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final ShopifySyncService syncService;

    public SyncController(ShopifySyncService syncService) {
        this.syncService = syncService;
    }

    // This is the endpoint your "Sync data" button will call
    @PostMapping("/now")
    public ResponseEntity<String> syncNow(@RequestParam Long storeId) {
        try {
            syncService.syncStore(storeId);
            return ResponseEntity.ok("Sync started & completed for storeId=" + storeId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Sync failed: " + e.getMessage());
        }
    }
}
