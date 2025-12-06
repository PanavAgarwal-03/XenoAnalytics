package com.panav.xenonovamart.repository;

import com.panav.xenonovamart.model.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    List<StoreEntity> findByOrganizationId(Long orgId);


    Optional<StoreEntity> findByShopDomain(String shopDomain);
}
