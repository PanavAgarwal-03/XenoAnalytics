package com.panav.xenonovamart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "orders",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "shopify_order_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "shopify_order_id", nullable = false)
    private Long shopifyOrderId;

    // NEW: link to Shopify customer
    @Column(name = "shopify_customer_id")
    private Long shopifyCustomerId;

    private Integer orderNumber;
    private BigDecimal totalPrice;
    private String currency;
    private OffsetDateTime createdAt;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "text")
    private String rawPayload;
}
