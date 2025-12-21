package com.giveitup.giveitup_be.dto.response;

import com.giveitup.giveitup_be.entity.NotificationEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long id;
    String message;
    String type;
    String link;
    boolean isRead;
    LocalDateTime createdAt;

    // Thông tin người gửi (để hiển thị avatar/tên người đã like/follow)
    Long senderId;
    String senderName;
    String senderAvatar;

    // Constructor nhận Entity để convert nhanh
    public NotificationResponse(NotificationEntity entity) {
        this.id = entity.getId();
        this.message = entity.getMessage();
        this.type = entity.getType();
        this.link = entity.getLink();
        this.isRead = entity.isRead();
        this.createdAt = entity.getCreatedAt();

        // Xử lý nếu có người gửi (nếu là thông báo hệ thống thì sender có thể null)
        if (entity.getSender() != null) {
            this.senderId = entity.getSender().getId();
            // Ưu tiên hiển thị tên đầy đủ, nếu không có thì lấy username
            this.senderName = (entity.getSender().getFirstName() != null && entity.getSender().getLastName() != null) 
                    ? entity.getSender().getFirstName() + " " + entity.getSender().getLastName() 
                    : entity.getSender().getUsername();
            this.senderAvatar = entity.getSender().getImageUser();
        } else {
            this.senderName = "Hệ thống"; // Hoặc "GiveItUp Admin"
            this.senderAvatar = null; // Hoặc set 1 ảnh logo mặc định
        }
    }
}