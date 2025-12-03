package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "payouts")
public class PayoutEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Bài post cần trao tiền
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    PostEntity post;

    // Số tiền admin chi
    @Column(nullable = false)
    Double amount;

    // Lời nhắn/ghi chú
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String note;

    // Trạng thái: PENDING, AUTHOR_CONFIRMED, REJECTED
    @Column(nullable = false)
    String status;

    // Admin tạo chi tiền
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserEntity user;

    // Author xác nhận
    LocalDateTime confirmedAt;

    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
}
