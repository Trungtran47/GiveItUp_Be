package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository  extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostIdAndParentCommentIsNull(Long postId);
    List<CommentEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<CommentEntity> findTop10ByOrderByCreatedAtDesc();
}
