package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "donations")
@EntityListeners(AuditingEntityListener.class)
public class DonateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    // Số tiền donate
    @Column(nullable = false)
    Double amount;
    // Ngày donate
    @Column(nullable = false)
    LocalDateTime donatedAt;
    // Status
    @Column(nullable = false)
    Long status;
    // Nội dung chuyển khoảng
    String title;
    // Người donate (nhiều donation có thể thuộc về 1 user)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;
    // Bài post được donate (1 bài có thể có nhiều donation)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    PostEntity post;
}
