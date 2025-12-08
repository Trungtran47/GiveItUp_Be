//package com.giveitup.giveitup_be.dto.response;
//
//import com.giveitup.giveitup_be.enums.UserStatus;
//import jakarta.persistence.Column;
//import lombok.*;
//import lombok.experimental.FieldDefaults;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.Set;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//@FieldDefaults(level = AccessLevel.PRIVATE)
//public class UserPostResponse {
//    Long id;
//    String username;
//    String firstName;
//    String lastName;
//    LocalDate dob;
//    Long gender;
//    String email;
//    String phoneNumber;
//    Long status;
//    String role;
//    String imageUser;
//    String organizationLogo;
//    String organizationLogoPublicId;
//    String organizationName;       // Tên tổ chức
//    CategoryResponse category;          // Lĩnh vực hoạt động
//    LocalDate establishmentDate;   // Thời gian thành lập
//    String organizationAddress;    // Địa điểm tổ chức
//    String organizationEmail;      // Email tổ chức
//    String registrationCode;       // Mã số đăng ký
//    String organizationPhone;      // Số điện thoại tổ chức
//    String verificationFile;       // Thông tin xác nhận / giấy tờ xác thực
//    String verificationInfoPublicId;
//    String linkInfoOrganization;   // link thông tin group/ fb
//    String organizationDescription; // mô ta tổ chức
//    LocalDateTime organizationCreatedAt; // Ngày nâng cấp tổ chức
//}
