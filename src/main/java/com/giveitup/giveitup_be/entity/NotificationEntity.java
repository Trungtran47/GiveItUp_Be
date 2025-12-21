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
@Table(name = "notification")
@EntityListeners(AuditingEntityListener.class)
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String message; // Nội dung: "A đã follow bạn"
    private String type; // FOLLOW, COMMENT, SYSTEM, POST...
    private String link; // Link để click vào (VD: /post/123)
    private boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    UserEntity recipient; // Người nhận thông báo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    UserEntity sender; // Người thực hiện hành động (có thể null nếu là System)
    @CreatedDate
    private LocalDateTime createdAt;
}