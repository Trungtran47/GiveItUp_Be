package com.giveitup.giveitup_be.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
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
    // (Tuỳ chọn) Danh mục bài đăng: Trẻ em, Bệnh tật, Thiên tai, Giáo dục,...
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonBackReference
    CategoryEntity category;
//    LocalDateTime startDate;
    LocalDateTime endDate;
    // Ảnh đại diện hoặc ảnh chính của bài đăng
    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    List<ImageEntity> images = new ArrayList<>();
    String video;
    String publicVideoId;

    // Địa chỉ
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String address;

    // Trạng thái bài đăng (VD: ACTIVE, CLOSED, PENDING)
    Long status;
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String statusName;

    @ManyToOne(fetch = FetchType.LAZY)
    BankAccountEntity bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    OrganizationEntity organization;

    // Thời gian tạo & cập nhật
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    LocalDateTime updatedAt;

    @Column(nullable = false)
    @ColumnDefault("0")
    private Long viewCount = 0L;
    @Column(nullable = false)
    @ColumnDefault("0")
    private Long likeCount  = 0L;
    // Danh sách chuyển khoản
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DonateEntity> donations = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<LikeEntity> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PostViewEntity> postViews = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PostUpdateEntity> updates = new ArrayList<>();
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PayoutEntity> payouts = new ArrayList<>();
}
