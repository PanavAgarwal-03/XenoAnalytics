package com.panav.xenonovamart.dto;

import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SyncStatusDto {
    private long totalCustomers;
    private long totalOrders;
    private long totalProducts;

    private OffsetDateTime lastOrderWebhookAt;
    private OffsetDateTime lastFullSyncAt;
}
