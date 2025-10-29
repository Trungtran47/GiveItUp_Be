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
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class PostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Tiêu đề bài đăng
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    String title;

    // Mô tả chi tiết bài đăng
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;

    // Ảnh đại diện hoặc ảnh chính của bài đăng
    @Column(columnDefinition = "NVARCHAR(500)")
    String imageUrl;

    // Tổng số tiền cần gây quỹ
    @Column(nullable = false)
    Double targetAmount;

    // Số tiền đã quyên góp được
    @Column(nullable = false)
    Double donatedAmount;

    // Ngày bắt đầu chiến dịch
    LocalDateTime startDate;

    // Ngày kết thúc chiến dịch
    LocalDateTime endDate;

    // Trạng thái bài đăng (VD: ACTIVE, CLOSED, PENDING)
    @Column(length = 50)
    String status;

    // Người tạo bài đăng (liên kết đến bảng User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserEntity user;

    // Thời gian tạo & cập nhật
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    // (Tuỳ chọn) Danh mục bài đăng: Trẻ em, Bệnh tật, Thiên tai, Giáo dục,...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    CategoryEntity category;
}
