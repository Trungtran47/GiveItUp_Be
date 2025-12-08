package com.giveitup.giveitup_be.dto.request;

import com.giveitup.giveitup_be.entity.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {
        Long id;
        String title;
        String description;
        Double targetAmount;
        String address;
//        Double donatedAmount;
        Long category;
//        LocalDateTime startDate;
        LocalDateTime endDate;
        List<ImageRequest> images;
        MultipartFile video;
        String publicVideoId;
        Long status;
        Long bankAccount; // 1 bài post có 1 ngân hàng
        Long user;
        // ==========================
        // ⭐ CLASS ẢNH LỒNG BÊN TRONG
        // ==========================
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @FieldDefaults(level = AccessLevel.PRIVATE)
        public static class ImageRequest {
                Long id;                 // ảnh cũ → dùng để giữ nguyên nếu cần
                String imageUrl;         // ảnh cũ
                String publicId;         // ảnh cũ
                MultipartFile file;      // ảnh mới được upload
                boolean isThumbnail;     // ảnh thumbnail FE chọn
        }
}
