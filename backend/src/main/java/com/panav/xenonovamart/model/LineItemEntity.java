package com.panav.xenonovamart.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(
        name = "line_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"shopify_line_item_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LineItemEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;  // FK to OrderEntity.id

    @Column(name = "shopify_line_item_id")
    private Long shopifyLineItemId;

    private Long productId;
    private Long variantId;
    private String title;
    private Integer quantity;
    private BigDecimal price;
    private String sku;
}
