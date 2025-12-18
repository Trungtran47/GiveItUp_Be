package com.giveitup.giveitup_be.entity;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostMapResponse {
    private Long id;
    private String title;
    private String address;
    private String image; // Trả thêm 1 ảnh thumbnail để hiển thị trên popup bản đồ cho đẹp
    private String categoryName;

    // Nếu sau này bạn lưu tọa độ vào DB thì dùng 2 trường này, hiện tại để null
    private Double latitude;
    private Double longitude;
}