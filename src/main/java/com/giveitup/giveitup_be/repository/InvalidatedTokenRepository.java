package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.InvalidatedTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedTokenEntity, String> {}
