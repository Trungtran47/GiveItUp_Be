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
    @ColumnDefault("0")
    Double donatedAmount = 0.0;
    // --- MỚI THÊM: Số lượt ủng hộ ---
    @Column(nullable = false)
    @ColumnDefault("0")
    Long donationCount = 0L;
    // --- MỚI THÊM: Tổng tiền đã rút/giải ngân ---
    @Column(nullable = false)
    @ColumnDefault("0")
    Double disbursedAmount = 0.0;
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
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String reason; // Lưu lý do từ chối
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

    // =================================================================
    // HELPER METHODS (Phương thức hỗ trợ logic nghiệp vụ)
    // =================================================================
    /**
     * Cập nhật số tiền đã rút (Dùng khi Admin chuyển khoản thành công)
     * @param amount Số tiền vừa rút trong đợt payout này
     */
    public void addDisbursedAmount(Double amount) {
        if (amount == null || amount <= 0) return;

        if (this.disbursedAmount == null) this.disbursedAmount = 0.0;
        this.disbursedAmount += amount;
    }
    /**
     * Tăng số lượt xem (View)
     */
    public void incrementViewCount() {
        if (this.viewCount == null) this.viewCount = 0L;
        this.viewCount++;
    }

    /**
     * Tăng số lượt thích (Like)
     */
    public void incrementLikeCount() {
        if (this.likeCount == null) this.likeCount = 0L;
        this.likeCount++;
    }

    /**
     * Giảm số lượt thích (Unlike) - Đảm bảo không âm
     */
    public void decrementLikeCount() {
        if (this.likeCount == null || this.likeCount <= 0) {
            this.likeCount = 0L;
        } else {
            this.likeCount--;
        }
    }

    /**
     * Xử lý khi có người ủng hộ (Donate)
     * Tăng số tiền đã quyên góp AND Tăng số lượt quyên góp
     * @param amount Số tiền ủng hộ
     */
    public void addDonation(Double amount) {
        if (amount == null || amount <= 0) return;

        // Xử lý tiền
        if (this.donatedAmount == null) this.donatedAmount = 0.0;
        this.donatedAmount += amount;

        // Xử lý số lượt (biến mới thêm)
        if (this.donationCount == null) this.donationCount = 0L;
        this.donationCount++;
    }

    /**
     * Xử lý hoàn tiền hoặc hủy quyên góp (nếu cần)
     * @param amount Số tiền hoàn lại
     */
    public void removeDonation(Double amount) {
        if (amount == null || amount <= 0) return;

        // Giảm tiền
        if (this.donatedAmount != null) {
            this.donatedAmount -= amount;
            if (this.donatedAmount < 0) this.donatedAmount = 0.0;
        }

        // Giảm số lượt
        if (this.donationCount != null && this.donationCount > 0) {
            this.donationCount--;
        }
    }
}
