package com.giveitup.giveitup_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorCreationRequest {
    MultipartFile organizationLogo;          // File ảnh đại diện
    String organizationLogoUrl;              // URL cũ (nếu cần)
    MultipartFile verificationFile;            // File xác thực
    String verificationFileUrl;                // URL cũ (nếu cần)
    String organizationName;       // Tên tổ chức
    Long category;          // Lĩnh vực hoạt động
    LocalDate establishmentDate;   // Thời gian thành lập
    String organizationAddress;    // Địa điểm tổ chức
    String organizationEmail;      // Email tổ chức
    String registrationCode;       // Mã số đăng ký
    String organizationPhone;      // Số điện thoại tổ chức
    String linkInfoOrganization;   // link thông tin group/ fb
    String organizationDescription; // mô ta tổ chức
}
