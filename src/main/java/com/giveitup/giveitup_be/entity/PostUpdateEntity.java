package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_updates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class PostUpdateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    String content;
    String imagePostUpdateUrl; // ảnh trao tiền
    String imagePostUpdatePublicId;

    // Thời gian tạo & cập nhật
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    PostEntity post;
    @OneToOne
    @JoinColumn(name = "payout_id", unique = true) // giữ khóa ngoại
    PayoutEntity payout;

}
