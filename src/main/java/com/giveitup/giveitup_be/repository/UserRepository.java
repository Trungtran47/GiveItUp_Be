package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);
}
