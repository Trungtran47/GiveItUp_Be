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
@Table(name = "search_history")
public class SearchHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Người dùng thực hiện tìm kiếm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    // Nội dung tìm kiếm
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    String keyword;

    // Loại tìm kiếm (tùy chọn: post, user, category…)
    @Column(nullable = true)
    String type;

    // Thời gian tạo
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
}
