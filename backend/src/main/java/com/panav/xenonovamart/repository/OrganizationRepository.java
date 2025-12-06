package com.panav.xenonovamart.repository;

import com.panav.xenonovamart.model.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
    Optional<OrganizationEntity> findByAdminEmail(String adminEmail);
}
