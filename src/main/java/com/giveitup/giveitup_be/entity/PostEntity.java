package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String description;
    @Column(nullable = false)
    Double targetAmount;
    @Column(nullable = false)
    Double donatedAmount;
    LocalDateTime startDate;
    LocalDateTime endDate;

    // Trạng thái bài đăng (VD: ACTIVE, CLOSED, PENDING)
    Long status;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    UserEntity user;

    // Thời gian tạo & cập nhật
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    LocalDateTime updatedAt;


    // (Tuỳ chọn) Danh mục bài đăng: Trẻ em, Bệnh tật, Thiên tai, Giáo dục,...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    CategoryEntity category;

    // Danh sách chuyển khoản
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DonateEntity> donations = new ArrayList<>();


    // Ảnh đại diện hoặc ảnh chính của bài đăng
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ImageEntity> images = new ArrayList<>();

}
