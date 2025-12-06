package com.panav.xenonovamart.repository;

import com.panav.xenonovamart.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByStoreIdAndShopifyProductId(Long storeId, Long shopifyProductId);
    long countByStoreId(Long storeId);
    List<ProductEntity> findByStoreIdOrderByTitleAsc(Long storeId);

}
