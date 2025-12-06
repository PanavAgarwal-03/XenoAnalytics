package com.panav.xenonovamart.repository;

import com.panav.xenonovamart.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    Optional<OrderEntity> findByStoreIdAndShopifyOrderId(Long storeId, Long shopifyOrderId);

    List<OrderEntity> findByStoreIdOrderByCreatedAtDesc(Long storeId);

    List<OrderEntity> findByStoreId(Long storeId);

    List<OrderEntity> findByStoreIdAndCreatedAtBetween(Long storeId, OffsetDateTime from, OffsetDateTime to);

    long countByStoreId(Long storeId);
}
