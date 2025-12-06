package com.panav.xenonovamart.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TopProductDto {
    private Long productId;
    private String title;
    private BigDecimal totalRevenue;
    private long quantitySold;
}
