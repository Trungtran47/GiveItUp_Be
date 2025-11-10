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
    String title;
    String description;
    Double targetAmount;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Long status;
    UserResponse user;
    CategoryResponse category;
    // Thời gian tạo & cập nhật
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<ImageEntity> images;
}
