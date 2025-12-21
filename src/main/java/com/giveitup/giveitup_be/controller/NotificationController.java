package com.giveitup.giveitup_be.controller;


import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.response.NotificationResponse;
import com.giveitup.giveitup_be.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // API: Lấy danh sách thông báo
    @GetMapping
    public ApiResponse<List<NotificationResponse>> getAll(@RequestParam Long userId) {
        return ApiResponse.<List<NotificationResponse>>builder()
                .result(notificationService.getAllNotifications(userId))
                .build();
    }

    // API: Đánh dấu đã đọc 1 cái
    @PutMapping("/{id}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ApiResponse.<String>builder().result("Đã cập nhật trạng thái").build();
    }

    // API: Đánh dấu tất cả đã đọc
    @PutMapping("/read-all")
    public ApiResponse<String> markAllAsRead(@RequestParam Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.<String>builder().result("Đã đọc tất cả").build();
    }
}