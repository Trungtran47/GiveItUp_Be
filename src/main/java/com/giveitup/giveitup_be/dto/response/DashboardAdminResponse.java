package com.giveitup.giveitup_be.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DashboardAdminResponse {

    // Section 0: Global Stats (Toàn hệ thống, không lọc ngày)
    private GlobalStatsDto globalStats;

    // Section 1: Performance Metrics (Có lọc ngày)
    private OverviewStatsDto overviewStats;

    // Section 2: Charts
    private List<FinanceChartDto> dataFinance;
    private List<CategoryChartDto> dataCategories;

    // Section 3: Lists
    private List<PayoutRequestDto> pendingPayouts;
    private List<ActivityDto> recentActivities;
    private List<TopPostDto> topPosts;

    /* --- INNER DTO CLASSES --- */

    @Data
    @Builder
    public static class GlobalStatsDto {
        private long totalUsers;
        private long totalAuthors; // Số lượng Organization
        private long totalPosts;
        private long activePosts;
        private long closedPosts;
        private long expiredPosts;
    }

    @Data
    @Builder
    public static class OverviewStatsDto {
        private double totalDonation;
        private long totalDonationCount;
        private double totalPayout;
        private long totalViews;
        private long totalLikes;

        // Chỉ số tăng trưởng (%)
        private double donationGrowth;
        private double donationCountGrowth;
        private double viewGrowth;
    }

    @Data
    @Builder
    public static class FinanceChartDto {
        private String name; // Label thời gian (T1, T2 hoặc 01/10...)
        private double donate;
        private double payout;
    }

    @Data
    @Builder
    public static class CategoryChartDto {
        private String name;
        private double value; // Số lượng bài viết hoặc %
    }

    @Data
    @Builder
    public static class PayoutRequestDto {
        private Long id;
        private String orgName;
        private Double amount;
        private String postTitle;
        private LocalDate requestDate;
        private String status; // String representation của Enum/Long
        private String type;
    }

    @Data
    @Builder
    public static class ActivityDto {
        private String type; // "update", "comment", "follow"
        private String user;
        private String content;
        private String time; // "2 giờ trước" - Xử lý ở Service
        private LocalDateTime createdTime; // Dùng để sort
    }

    @Data
    @Builder
    public static class TopPostDto {
        private Long key; // Post ID
        private String title;
        private Double target;
        private Double current;
        private String status;
        private Long views;
        private Long likes;
    }
}