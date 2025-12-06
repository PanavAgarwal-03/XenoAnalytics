package com.panav.xenonovamart.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "customers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"storeId", "shopify_customer_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long storeId;
    private Long orgId;

    @Column(name = "shopify_customer_id")
    private Long shopifyCustomerId;

    private String email;
    private String firstName;
    private String lastName;

    @Lob
    @Column(columnDefinition = "text")
    private String defaultAddressJson;
}
