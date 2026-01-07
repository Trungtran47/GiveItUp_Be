package com.giveitup.giveitup_be.service; // Đổi theo package của bạn

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // --- 1. XỬ LÝ VIEW (XEM) ---
    public void saveViewHistory(Long userId, Long postId) {
        String key = "user:" + userId + ":views"; // Key riêng cho View
        saveToZSet(key, postId, 50); // Lưu tối đa 50 bài xem gần nhất
        System.out.println(">> REDIS: User " + userId + " VIEWED Post " + postId);
    }

    // --- 2. XỬ LÝ LIKE (THÍCH) ---
    public void saveLikeHistory(Long userId, Long postId) {
        String key = "user:" + userId + ":likes"; // Key riêng cho Like
        saveToZSet(key, postId, 100); // Lưu tối đa 100 bài like gần nhất
        System.out.println(">> REDIS: User " + userId + " LIKED Post " + postId);
    }

    // --- 3. XỬ LÝ UNLIKE (BỎ THÍCH) ---
    public void removeLikeHistory(Long userId, Long postId) {
        String key = "user:" + userId + ":likes";
        String value = String.valueOf(postId);
        redisTemplate.opsForZSet().remove(key, value); // Xóa khỏi Redis
        System.out.println(">> REDIS: User " + userId + " UNLIKED Post " + postId);
    }

    // --- Hàm phụ trợ (Common logic) ---
    private void saveToZSet(String key, Long postId, int limit) {
        String value = String.valueOf(postId);
        double score = System.currentTimeMillis();

        // Add hoặc Update score (thời gian)
        redisTemplate.opsForZSet().add(key, value, score);

        // Giới hạn số lượng (Xóa các bài cũ quá)
        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > limit) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - (limit + 1));
        }
        // Set hết hạn 30 ngày
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
    }
    //  XỬ LÝ SEARCH (Lưu các bài viết tìm thấy) ---
    public void saveSearchHistory(Long userId, List<Long> postIds) {
        String key = "user:" + userId + ":searches";
        double score = System.currentTimeMillis();

        // Lưu từng bài viết tìm được vào Redis
        for (Long pid : postIds) {
            redisTemplate.opsForZSet().add(key, String.valueOf(pid), score);
        }

        // Giới hạn lưu 50 bài tìm kiếm gần nhất để tiết kiệm RAM
        Long size = redisTemplate.opsForZSet().size(key);
        if (size != null && size > 50) {
            redisTemplate.opsForZSet().removeRange(key, 0, size - 51);
        }
        redisTemplate.expire(key, 30, TimeUnit.DAYS);

        System.out.println(">> REDIS: User " + userId + " SEARCHED & FOUND " + postIds.size() + " posts");
    }
}