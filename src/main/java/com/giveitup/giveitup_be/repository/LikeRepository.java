package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.LikeEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {
    LikeEntity findLikeEntitiesByPostIdAndUserId(Long postId, Long userId);
    List<LikeEntity> findAllByUser(UserEntity user);
    // Kiểm tra xem user đã like bài chưa
    boolean existsByPostIdAndUserId(Long postId, Long userId);
}
