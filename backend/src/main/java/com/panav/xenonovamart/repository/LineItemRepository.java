
package com.panav.xenonovamart.repository;

import com.panav.xenonovamart.model.LineItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LineItemRepository extends JpaRepository<LineItemEntity, Long> {

    List<LineItemEntity> findByOrderIdIn(List<Long> orderIds);
}

