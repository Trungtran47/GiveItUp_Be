package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.CommentReactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentReactionRepository extends JpaRepository<CommentReactionEntity, Long> {
    // Tìm tương tác của user với comment cụ thể
    Optional<CommentReactionEntity> findByUserIdAndCommentId(Long userId, Long commentId);
    @Query("SELECT r FROM CommentReactionEntity r WHERE r.user.id = :userId AND r.comment.post.id = :postId")
    List<CommentReactionEntity> findAllByUserIdAndPostId(Long userId, Long postId);
}