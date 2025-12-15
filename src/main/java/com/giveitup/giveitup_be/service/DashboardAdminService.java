package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.response.DashboardAdminResponse;
import com.giveitup.giveitup_be.dto.response.DashboardAdminResponse.*;
import com.giveitup.giveitup_be.entity.*;
import com.giveitup.giveitup_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardAdminService {

    private final PostRepository postRepository;
    private final DonateRepository donateRepository;
    private final PayoutRepository payoutRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PostViewRepository postViewRepository;
    private final LikeRepository likeRepository;

    // Activity Repos
    private final CommentRepository commentRepository;
    private final PostUpdateRepository postUpdateRepository;
    private final FollowRepository followRepository;

    // Constants cho Status
    private final Long STATUS_ACTIVE = 1L;
    private final Long STATUS_CLOSED = 2L;
    private final Long STATUS_PAYOUT_PENDING = 1L;
    private final Long STATUS_PAYOUT_APPROVED = 4L;

    public DashboardAdminResponse getDashboardData(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(23, 59, 59);

        // 1. Lấy Global Stats
        GlobalStatsDto globalStats = GlobalStatsDto.builder()
                .totalUsers(userRepository.count())
                .totalAuthors(organizationRepository.count())
                .totalPosts(postRepository.count())
                .activePosts(postRepository.countByStatus(STATUS_ACTIVE))
                .closedPosts(postRepository.countByStatus(STATUS_CLOSED))
                .expiredPosts(postRepository.countExpiredPosts())
                .build();

        // 2. Lấy Overview Stats & Growth
        OverviewStatsDto overviewStats = calculateOverviewStats(start, end);

        // 3. Lấy Chart Data
        List<FinanceChartDto> financeChart = getFinanceChartData(start, end);
        List<CategoryChartDto> categoryChart = getCategoryChartData();

        // 4. Lấy Lists
        List<PayoutRequestDto> pendingPayouts = getPendingPayouts();
        List<ActivityDto> activities = getRecentActivities();
        List<TopPostDto> topPosts = getTopPosts();

        return DashboardAdminResponse.builder()
                .globalStats(globalStats)
                .overviewStats(overviewStats)
                .dataFinance(financeChart)
                .dataCategories(categoryChart)
                .pendingPayouts(pendingPayouts)
                .recentActivities(activities)
                .topPosts(topPosts)
                .build();
    }

    // --- HELPER METHODS ---

    private OverviewStatsDto calculateOverviewStats(LocalDateTime start, LocalDateTime end) {
        long daysDiff = ChronoUnit.DAYS.between(start, end);
        LocalDateTime prevStart = start.minusDays(daysDiff);
        LocalDateTime prevEnd = start.minusSeconds(1);

        // Current metrics
        Double currentDonation = donateRepository.sumAmountBetween(start, end);
        long currentDonationCount = donateRepository.countByCreatedAtBetween(start, end);
        Double currentPayout = payoutRepository.sumPayoutByStatusBetween(STATUS_PAYOUT_APPROVED, start, end);
        Long currentViews = postViewRepository.sumViewsBetween(start, end);
        long currentLikes = likeRepository.countByCreatedAtBetween(start, end);

        // Previous metrics
        Double prevDonation = donateRepository.sumAmountBetween(prevStart, prevEnd);
        long prevDonationCount = donateRepository.countByCreatedAtBetween(prevStart, prevEnd);
        Long prevViews = postViewRepository.sumViewsBetween(prevStart, prevEnd);

        return OverviewStatsDto.builder()
                .totalDonation(currentDonation)
                .totalDonationCount(currentDonationCount)
                .totalPayout(currentPayout)
                .totalViews(currentViews)
                .totalLikes(currentLikes)
                .donationGrowth(calculateGrowth(currentDonation, prevDonation))
                .donationCountGrowth(calculateGrowth((double) currentDonationCount, (double) prevDonationCount))
                .viewGrowth(calculateGrowth((double) currentViews, (double) prevViews))
                .build();
    }

    private double calculateGrowth(Double current, Double previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((current - previous) / previous) * 100.0;
    }

    private List<FinanceChartDto> getFinanceChartData(LocalDateTime start, LocalDateTime end) {
        List<Object[]> donations = donateRepository.getDailyDonationStats(start, end);
        List<Object[]> payouts = payoutRepository.getDailyPayoutStats(STATUS_PAYOUT_APPROVED, start, end);

        Map<String, FinanceChartDto> chartMap = new TreeMap<>();

        for (Object[] row : donations) {
            String date = row[0].toString();
            Double amount = ((Number) row[1]).doubleValue();
            chartMap.put(date, FinanceChartDto.builder().name(date).donate(amount).payout(0).build());
        }

        for (Object[] row : payouts) {
            String date = row[0].toString();
            Double amount = ((Number) row[1]).doubleValue();
            FinanceChartDto dto = chartMap.getOrDefault(date, FinanceChartDto.builder().name(date).donate(0).payout(0).build());
            dto.setPayout(amount);
            chartMap.put(date, dto);
        }

        return new ArrayList<>(chartMap.values());
    }

    private List<CategoryChartDto> getCategoryChartData() {
        List<Object[]> results = postRepository.countPostsByCategory();
        return results.stream().map(row -> CategoryChartDto.builder()
                .name((String) row[0])
                .value(((Number) row[1]).doubleValue())
                .build()).collect(Collectors.toList());
    }

    private List<PayoutRequestDto> getPendingPayouts() {
        return payoutRepository.findByStatus(STATUS_PAYOUT_PENDING).stream()
                .map(p -> PayoutRequestDto.builder()
                        .id(p.getId())
                        .amount(p.getAmount())
                        .orgName(p.getRequestedBy() != null ? p.getRequestedBy().getOrganizationName() : "N/A")
                        .postTitle(p.getPost() != null ? p.getPost().getTitle() : "N/A")
                        .requestDate(p.getRequestedAt() != null ? p.getRequestedAt().toLocalDate() : null)
                        .status("PENDING")
                        .type(p.getType())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ActivityDto> getRecentActivities() {
        List<ActivityDto> activities = new ArrayList<>();

        // 1. Comment Activity
        commentRepository.findTop10ByOrderByCreatedAtDesc().forEach(c ->
                activities.add(ActivityDto.builder()
                        .type("comment")
                        .user(getDisplayName(c.getUser())) // <--- SỬ DỤNG HÀM XỬ LÝ TÊN MỚI
                        .content("Đã bình luận: " + c.getContent())
                        .createdTime(c.getCreatedAt())
                        .build())
        );

        // 2. Update Activity
        postUpdateRepository.findTop10ByOrderByCreatedAtDesc().forEach(u -> {
            // Lấy tên Author từ bài viết (Organization của bài post)
            String authorName = "N/A";
            if (u.getPost() != null && u.getPost().getOrganization() != null) {
                authorName = u.getPost().getOrganization().getOrganizationName();
            }

            activities.add(ActivityDto.builder()
                    .type("update")
                    .user(authorName)
                    .content(u.getContent())
                    .createdTime(u.getCreatedAt())
                    .build());
        });

        // 3. Follow Activity
        followRepository.findTop10ByOrderByCreatedAtDesc().forEach(f ->
                activities.add(ActivityDto.builder()
                        .type("follow")
                        .user(getDisplayName(f.getFollower())) // <--- SỬ DỤNG HÀM XỬ LÝ TÊN MỚI
                        .content("Đã theo dõi " + getDisplayName(f.getFollowing()))
                        .createdTime(f.getCreatedAt())
                        .build())
        );

        // Sort và format
        activities.sort((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()));
        activities.forEach(a -> a.setTime(a.getCreatedTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))));

        return activities.stream().limit(10).collect(Collectors.toList());
    }

    private List<TopPostDto> getTopPosts() {
        return postRepository.findTopPerformingPosts(PageRequest.of(0, 5)).stream()
                .map(p -> TopPostDto.builder()
                        .key(p.getId())
                        .title(p.getTitle())
                        .target(p.getTargetAmount())
                        .current(p.getDonatedAmount())
                        .status(p.getStatusName())
                        .views(p.getViewCount())
                        .likes(p.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }

    // --- LOGIC HIỂN THỊ TÊN NGƯỜI DÙNG ---
    private String getDisplayName(UserEntity user) {
        if (user == null) return "Người dùng ẩn danh";

        // 1. Kiểm tra nếu là Author (có Organization liên kết)
        // Giả sử UserEntity có quan hệ với OrganizationEntity qua field 'organization'
        // Bạn hãy đảm bảo UserEntity có getter getOrganization()
        try {
            // Dùng reflection hoặc check null thông thường nếu bạn có field này
            if (user.getOrganization() != null) {
                return user.getOrganization().getOrganizationName();
            }
        } catch (Exception e) {
            // Bỏ qua nếu UserEntity chưa có field organization
        }

        // 2. Nếu không phải Author -> Lấy First Name + Last Name
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();

        // 3. Fallback: Nếu tên rỗng thì lấy username
        return fullName.isEmpty() ? user.getUsername() : fullName;
    }
}