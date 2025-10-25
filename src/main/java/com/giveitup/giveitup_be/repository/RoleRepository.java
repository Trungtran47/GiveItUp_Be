package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {}
