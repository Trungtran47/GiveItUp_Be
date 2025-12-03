package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "comments")
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Nội dung bình luận
    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    String content;

    // Người bình luận
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserEntity user;

    // Bài post
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    PostEntity post;

    // Reply cho comment khác
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    CommentEntity parentComment;

    // Danh sách reply
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    List<CommentEntity> replies;

    // Thời gian tạo
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
}
