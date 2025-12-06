package com.panav.xenonovamart.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdersByDateDto {
    private LocalDate date;
    private long orderCount;
    private BigDecimal totalRevenue;
}
