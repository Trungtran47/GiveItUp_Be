package com.giveitup.giveitup_be.entity;

import com.giveitup.giveitup_be.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "username", unique = true, columnDefinition = "NVARCHAR(255)")
    String username;
    String password;
    @Column(columnDefinition = "NVARCHAR(255)")
    String firstName;
    @Column(columnDefinition = "NVARCHAR(255)")
    String lastName;
    LocalDate dob;
    String email;
    String phoneNumber;
    Long gender;
    @Column( columnDefinition = "NVARCHAR(255)")
    String address;
    String imageUser;
    String publicImageUserId;
    //  Thông tin cho tổ chức hoặc nhóm từ thiện
    Long status = (long) UserStatus.USER.getCode();
    String organizationLogo;
    String organizationLogoPublicId;
    @Column(columnDefinition = "NVARCHAR(255)")
    String organizationName;       // Tên tổ chức
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    CategoryEntity category;         // Lĩnh vực hoạt động
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
    LocalDateTime organizationCreatedAt; // Ngày nâng cấp tổ chức

    // Thời gian tạo & cập nhật
    @CreatedDate
    @Column(updatable = false)
    LocalDateTime createdAt;
    @LastModifiedDate
    LocalDateTime updatedAt;
    //    @ManyToMany
//    Set<RoleEntity> roleEntities;
    @ManyToOne(fetch = FetchType.LAZY)
     RoleEntity role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
     Set<PostEntity> posts;
    // Danh sách chuển khoản đã ủng hộ cho bài post
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<DonateEntity> donations = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    List<BankAccountEntity> bankAccounts ;
    // bản like
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<LikeEntity> likes = new ArrayList<>();
    // view
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PostViewEntity> postViews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PayoutEntity> payouts = new ArrayList<>();

    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowEntity> following = new ArrayList<>();

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FollowEntity> followers = new ArrayList<>();

}
