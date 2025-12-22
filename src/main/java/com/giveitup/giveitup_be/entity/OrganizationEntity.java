package com.giveitup.giveitup_be.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String organizationLogo;
    String organizationLogoPublicId;
    @Column(columnDefinition = "NVARCHAR(255)")
    String organizationName;       // Tên tổ chức
  // Lĩnh vực hoạt động
    LocalDate establishmentDate;   // Thời gian thành lập
    @Column(columnDefinition = "NVARCHAR(255)")
    String organizationAddress;    // Địa điểm tổ chức
    String organizationEmail;      // Email tổ chức
    String registrationCode;       // Mã số đăng ký
    String organizationPhone;      // Số điện thoại tổ chức
    String verificationFile;       // Thông tin xác nhận / giấy tờ xác thực
    String verificationInfoPublicId;
    String linkInfoOrganization;   // link thông tin group/ fb
    @Column(columnDefinition = "NVARCHAR(MAX)")
    String organizationDescription; // mô ta tổ chức
    LocalDateTime organizationApprovedAt; // Ngày được duyệt lên tổ chức

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    Set<PostEntity> posts;

    @OneToMany(mappedBy = "organization", fetch = FetchType.LAZY)
    List<BankAccountEntity> bankAccounts ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    CategoryEntity category;
    @OneToMany(mappedBy = "requestedBy", cascade = CascadeType.ALL)
    List<PayoutEntity> payoutRequests = new ArrayList<>();
    /* ===== LINK USER ===== */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    UserEntity user;


    // Thời gian tạo & cập nhật
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    LocalDateTime updatedAt;
}
