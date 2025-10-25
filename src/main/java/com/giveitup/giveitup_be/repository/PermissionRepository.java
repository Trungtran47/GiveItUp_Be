package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}
