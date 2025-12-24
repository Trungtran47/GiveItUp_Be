package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.PostViewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostViewRepository extends JpaRepository<PostViewEntity, Long> {
    Optional<PostViewEntity> findFirstByPostIdAndUserId(Long postId, Long userId);
    Optional<PostViewEntity> findByPostIdAndUserId(Long postId, Long userId);
    @Query("SELECT COALESCE(SUM(v.viewCount), 0) FROM PostViewEntity v WHERE v.createdAt BETWEEN :start AND :end")
    Long sumViewsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
