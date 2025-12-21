package com.giveitup.giveitup_be.dto.response;

import com.giveitup.giveitup_be.entity.ImageEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostResponse {
    Long id;
    String title;
    String description;
    Double targetAmount;
    Double donatedAmount;
    Long viewCount;
    LocalDateTime viewAt;
    Long likeCount;
    boolean liked;
    LocalDateTime likedAt;
//    LocalDateTime startDate;
    LocalDateTime endDate;
    Long status;
    String statusName;
    String reason;
    BankAccountResponse bankAccount;
    OrganizationResponse organization;
    CategoryResponse category;
    String address;
    // Thời gian tạo & cập nhật
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<ImageEntity> images;
    String video;
    String publicVideoId;
    List<PayoutResponse> payouts;
}
