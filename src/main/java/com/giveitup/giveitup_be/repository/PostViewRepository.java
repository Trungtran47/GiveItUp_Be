package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.PostViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostViewEntity, Long> {
    Optional<PostViewEntity> findByPostIdAndUserId(Long postId, Long userId);
}
