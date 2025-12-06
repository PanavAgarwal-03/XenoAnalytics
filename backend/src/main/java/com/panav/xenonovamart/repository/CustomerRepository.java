package com.panav.xenonovamart.repository;

import com.panav.xenonovamart.model.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByStoreIdAndShopifyCustomerId(Long storeId, Long shopifyCustomerId);
    long countByStoreId(long storeId);
    List<CustomerEntity> findByStoreId(Long storeId);

}
