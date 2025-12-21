package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.response.NotificationResponse;
import com.giveitup.giveitup_be.entity.NotificationEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    public void sendNotification(UserEntity recipient, UserEntity sender, String message, String type, String link) {
        // 1. Lưu vào Database
        NotificationEntity notification = NotificationEntity.builder()
                .recipient(recipient)
                .sender(sender)
                .message(message)
                .type(type)
                .link(link)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);

        // 2. Tạo DTO để gửi ra ngoài (tránh lộ thông tin user entity)
        NotificationResponse dto = new NotificationResponse(notification);

        // 3. Bắn message tới đường dẫn riêng của user đó
        // Client sẽ subscribe vào: /topic/notifications/{userId}
        messagingTemplate.convertAndSend("/topic/notifications/" + recipient.getId(), dto);
    }
    // 1. LẤY TẤT CẢ THÔNG BÁO CỦA USER
    public List<NotificationResponse> getAllNotifications(Long userId) {
        List<NotificationEntity> list = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId);
        return list.stream()
                .map(NotificationResponse::new) // Convert Entity sang DTO
                .collect(Collectors.toList());
    }
    // 2. ĐÁNH DẤU ĐÃ ĐỌC (CẬP NHẬT DB)
    public void markAsRead(Long notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    // 3. ĐÁNH DẤU TẤT CẢ LÀ ĐÃ ĐỌC (Optional)
    public void markAllAsRead(Long userId) {
        List<NotificationEntity> list = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(userId);
        for (NotificationEntity noti : list) {
            noti.setRead(true);
        }
        notificationRepository.saveAll(list);
    }
}