package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findAllByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    // Lấy danh sách thông báo của user, sắp xếp mới nhất lên đầu
    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // Đếm số lượng thông báo chưa đọc của user (để hiển thị badge số đỏ trên icon chuông)
    long countByRecipientIdAndIsReadFalse(Long recipientId);
}