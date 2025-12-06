package com.panav.xenonovamart.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "stores", uniqueConstraints = @UniqueConstraint(columnNames = {"shop_domain"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_domain", nullable = false)
    private String shopDomain;

    @JsonIgnore      // 👈 add this
    @Column(name = "access_token")
    private String accessToken;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    private OffsetDateTime lastOrderWebhookAt;
    private OffsetDateTime lastFullSyncAt;
}

