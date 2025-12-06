package com.panav.xenonovamart.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MetricsSummaryDto {
    private long totalCustomers;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private long newCustomers;
    private long returningCustomers;
}
