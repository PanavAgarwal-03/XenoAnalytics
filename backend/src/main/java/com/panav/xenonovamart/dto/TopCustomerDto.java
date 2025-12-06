package com.panav.xenonovamart.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TopCustomerDto {
    private Long customerId;
    private String email;
    private String name;
    private BigDecimal totalSpend;
    private long orderCount;
}
