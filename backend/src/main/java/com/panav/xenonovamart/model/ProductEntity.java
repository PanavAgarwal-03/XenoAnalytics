package com.panav.xenonovamart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "shopify_product_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "org_id")
    private Long orgId;

    @Column(name = "shopify_product_id", nullable = false)
    private Long shopifyProductId;

    private String title;
    private String status;          // active / draft / archived
    private String productType;     // e.g. "Fitness"
    private String vendor;          // "XenoNovaMart"

    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer totalVariants;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @JsonIgnore
    @Lob
    @Column(columnDefinition = "text")
    private String rawPayload;
}
