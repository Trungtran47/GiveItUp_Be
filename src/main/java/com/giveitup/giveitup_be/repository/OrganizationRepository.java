package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {
    long count(); // Tổng tổ chức
}
