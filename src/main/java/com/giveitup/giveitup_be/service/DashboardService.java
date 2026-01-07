//package com.giveitup.giveitup_be.service;
//
//import com.giveitup.giveitup_be.dto.response.DashboardResponse;
//import com.giveitup.giveitup_be.entity.UserEntity;
//import com.giveitup.giveitup_be.exception.AppException;
//import com.giveitup.giveitup_be.exception.ErrorCode;
//import com.giveitup.giveitup_be.projection.DashboardSummaryProjection;
//import com.giveitup.giveitup_be.repository.PostRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.YearMonth;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//public class DashboardService {
//
//    private final PostRepository postRepository;
//    private final UserService userService;
//
//    @PreAuthorize("hasRole('AUTHOR')")
//    public DashboardResponse dashboardAuthor(
//            String mode,
//            LocalDate date,
//            Integer year,
//            Integer month
//    ) {
//        if (mode == null) throw new AppException(ErrorCode.INVALID_REQUEST);
//        mode = mode.toUpperCase();
//
//        UserEntity user = userService.getMyInfoReturnEntity();
//        Long authorId = user.getOrganization().getId();
//
//        LocalDateTime curStart, curEnd, preStart, preEnd;
//        String currentLabel, previousLabel;
//        List<DashboardResponse.Chart> chartList;
//
//        switch (mode) {
//            /* ================= DAY ================= */
//            case "DAY" -> {
//                if (date == null) throw new AppException(ErrorCode.INVALID_REQUEST);
//
//                LocalDate cur = date;
//                LocalDate pre = date.minusDays(1);
//
//                curStart = cur.atStartOfDay();
//                curEnd = cur.atTime(23, 59, 59);
//                preStart = pre.atStartOfDay();
//                preEnd = pre.atTime(23, 59, 59);
//
//                currentLabel = cur.toString();
//                previousLabel = pre.toString();
//
//                chartList = fillChartDay(authorId, curStart, curEnd);
//            }
//
//            /* ================= MONTH ================= */
//            case "MONTH" -> {
//                if (year == null || month == null) throw new AppException(ErrorCode.INVALID_REQUEST);
//
//                YearMonth cur = YearMonth.of(year, month);
//                YearMonth pre = cur.minusMonths(1);
//
//                curStart = cur.atDay(1).atStartOfDay();
//                curEnd = cur.atEndOfMonth().atTime(23, 59, 59);
//                preStart = pre.atDay(1).atStartOfDay();
//                preEnd = pre.atEndOfMonth().atTime(23, 59, 59);
//
//                currentLabel = cur.toString();
//                previousLabel = pre.toString();
//
//                chartList = fillChartMonth(authorId, curStart, curEnd);
//            }
//
//            /* ================= YEAR ================= */
//            case "YEAR" -> {
//                if (year == null) throw new AppException(ErrorCode.INVALID_REQUEST);
//
//                curStart = LocalDate.of(year, 1, 1).atStartOfDay();
//                curEnd = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
//                preStart = curStart.minusYears(1);
//                preEnd = curEnd.minusYears(1);
//
//                currentLabel = String.valueOf(year);
//                previousLabel = String.valueOf(year - 1);
//
//                chartList = fillChartYear(authorId, curStart, curEnd);
//            }
//
//            default -> throw new AppException(ErrorCode.INVALID_REQUEST);
//        }
//
//        DashboardSummaryProjection currentSummary = postRepository.summaryByAuthor(authorId, curStart, curEnd);
//        DashboardSummaryProjection previousSummary = postRepository.summaryByAuthor(authorId, preStart, preEnd);
//
//        return DashboardResponse.builder()
//                .current(mapSummary(currentSummary, currentLabel))
//                .previous(mapSummary(previousSummary, previousLabel))
//                .chart(chartList)
//                .build();
//    }
//
//    /* ==================== HELPERS ==================== */
//    private DashboardResponse.Summary mapSummary(DashboardSummaryProjection p, String time) {
//        return DashboardResponse.Summary.builder()
//                .time(time)
//                .totalPost(p.getTotalPost())
//                .donateCount(p.getDonateCount())
//                .totalDonated(p.getTotalDonated())
//                .totalView(p.getTotalView())
//                .totalLike(p.getTotalLike())
//                .totalComment(p.getTotalComment())
//                .build();
//    }
//
//    private List<DashboardResponse.Chart> fillChartDay(Long authorId, LocalDateTime start, LocalDateTime end) {
//        Map<String, DashboardResponse.Chart> map = new LinkedHashMap<>();
//        for (int h = 0; h < 24; h++) {
//            String hour = String.format("%02d", h);
//            map.put(hour, DashboardResponse.Chart.builder()
//                    .time(hour)
//                    .donateCount(0L)
//                    .totalDonated(0.0)
//                    .totalView(0L)
//                    .totalLike(0L)
//                    .totalComment(0L)
//                    .build());
//        }
//
//        List<Map<String, Object>> raw = postRepository.chartDayByAuthor(authorId, start, end);
//        for (Map<String, Object> r : raw) {
//            String hour = (String) r.get("time");
//            map.put(hour, DashboardResponse.Chart.builder()
//                    .time(hour)
//                    .donateCount(((Number) r.get("donateCount")).longValue())
//                    .totalDonated(((Number) r.get("totalDonated")).doubleValue())
//                    .totalView(((Number) r.get("totalView")).longValue())
//                    .totalLike(((Number) r.get("totalLike")).longValue())
//                    .totalComment(((Number) r.get("totalComment")).longValue())
//                    .build());
//        }
//        return new ArrayList<>(map.values());
//    }
//
//    private List<DashboardResponse.Chart> fillChartMonth(Long authorId, LocalDateTime start, LocalDateTime end) {
//        Map<String, DashboardResponse.Chart> map = new LinkedHashMap<>();
//        LocalDate d = start.toLocalDate();
//        while (!d.isAfter(end.toLocalDate())) {
//            String day = d.toString();
//            map.put(day, DashboardResponse.Chart.builder()
//                    .time(day)
//                    .donateCount(0L)
//                    .totalDonated(0.0)
//                    .totalView(0L)
//                    .totalLike(0L)
//                    .totalComment(0L)
//                    .build());
//            d = d.plusDays(1);
//        }
//
//        List<Map<String, Object>> raw = postRepository.chartMonthByAuthor(authorId, start, end);
//        for (Map<String, Object> r : raw) {
//            String day = (String) r.get("time");
//            map.put(day, DashboardResponse.Chart.builder()
//                    .time(day)
//                    .donateCount(((Number) r.get("donateCount")).longValue())
//                    .totalDonated(((Number) r.get("totalDonated")).doubleValue())
//                    .totalView(((Number) r.get("totalView")).longValue())
//                    .totalLike(((Number) r.get("totalLike")).longValue())
//                    .totalComment(((Number) r.get("totalComment")).longValue())
//                    .build());
//        }
//        return new ArrayList<>(map.values());
//    }
//
//    private List<DashboardResponse.Chart> fillChartYear(Long authorId, LocalDateTime start, LocalDateTime end) {
//        Map<String, DashboardResponse.Chart> map = new LinkedHashMap<>();
//        int year = start.getYear();
//        for (int m = 1; m <= 12; m++) {
//            String monthStr = String.format("%04d-%02d", year, m);
//            map.put(monthStr, DashboardResponse.Chart.builder()
//                    .time(monthStr)
//                    .donateCount(0L)
//                    .totalDonated(0.0)
//                    .totalView(0L)
//                    .totalLike(0L)
//                    .totalComment(0L)
//                    .build());
//        }
//
//        List<Map<String, Object>> raw = postRepository.chartYearByAuthor(authorId, start, end);
//        for (Map<String, Object> r : raw) {
//            String monthStr = (String) r.get("time");
//            map.put(monthStr, DashboardResponse.Chart.builder()
//                    .time(monthStr)
//                    .donateCount(((Number) r.get("donateCount")).longValue())
//                    .totalDonated(((Number) r.get("totalDonated")).doubleValue())
//                    .totalView(((Number) r.get("totalView")).longValue())
//                    .totalLike(((Number) r.get("totalLike")).longValue())
//                    .totalComment(((Number) r.get("totalComment")).longValue())
//                    .build());
//        }
//        return new ArrayList<>(map.values());
//    }
//
//}
package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.response.DashboardResponse;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.projection.DashboardSummaryProjection;
import com.giveitup.giveitup_be.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final PostRepository postRepository;
    private final UserService userService;

    // Class nội bộ (hoặc record) để lưu khoảng thời gian
    private record DateRange(LocalDateTime start, LocalDateTime end, String label) {}

    @PreAuthorize("hasRole('AUTHOR')")
    public DashboardResponse dashboardAuthor(
            String mode,
            LocalDate date,
            Integer year,
            Integer month
    ) {
        if (mode == null) throw new AppException(ErrorCode.INVALID_REQUEST);
        String finalMode = mode.toUpperCase();

        // 1. Lấy Author ID (Phải lấy ở luồng chính trước khi async)
        UserEntity user = userService.getMyInfoReturnEntity();
        Long authorId = user.getOrganization().getId();

        // 2. Tính toán ngày tháng
        DateRange curRange;
        DateRange preRange;

        switch (finalMode) {
            case "DAY" -> {
                if (date == null) throw new AppException(ErrorCode.INVALID_REQUEST);
                LocalDate cur = date;
                LocalDate pre = date.minusDays(1);
                curRange = new DateRange(cur.atStartOfDay(), cur.atTime(23, 59, 59), cur.toString());
                preRange = new DateRange(pre.atStartOfDay(), pre.atTime(23, 59, 59), pre.toString());
            }
            case "MONTH" -> {
                if (year == null || month == null) throw new AppException(ErrorCode.INVALID_REQUEST);
                YearMonth cur = YearMonth.of(year, month);
                YearMonth pre = cur.minusMonths(1);
                curRange = new DateRange(cur.atDay(1).atStartOfDay(), cur.atEndOfMonth().atTime(23, 59, 59), cur.toString());
                preRange = new DateRange(pre.atDay(1).atStartOfDay(), pre.atEndOfMonth().atTime(23, 59, 59), pre.toString());
            }
            case "YEAR" -> {
                if (year == null) throw new AppException(ErrorCode.INVALID_REQUEST);
                LocalDateTime curStart = LocalDate.of(year, 1, 1).atStartOfDay();
                LocalDateTime curEnd = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
                curRange = new DateRange(curStart, curEnd, String.valueOf(year));
                LocalDateTime preStart = curStart.minusYears(1);
                LocalDateTime preEnd = curEnd.minusYears(1);
                preRange = new DateRange(preStart, preEnd, String.valueOf(year - 1));
            }
            default -> throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 3. XỬ LÝ SONG SONG (PARALLEL EXECUTION)

        // Task 1: Lấy Chart Data (Chạy luồng riêng)
        CompletableFuture<List<DashboardResponse.Chart>> chartFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return getChartData(authorId, finalMode, curRange);
            } catch (Exception e) {
                log.error("Error fetching chart data", e);
                return new ArrayList<>(); // Trả về list rỗng nếu lỗi
            }
        });

        // Task 2: Lấy Summary Hiện tại (Chạy luồng riêng)
        CompletableFuture<DashboardResponse.Summary> currentSummaryFuture = CompletableFuture.supplyAsync(() -> {
            DashboardSummaryProjection p = postRepository.summaryByAuthor(authorId, curRange.start(), curRange.end());
            return mapSummary(p, curRange.label());
        });

        // Task 3: Lấy Summary Quá khứ (Chạy luồng riêng)
        CompletableFuture<DashboardResponse.Summary> previousSummaryFuture = CompletableFuture.supplyAsync(() -> {
            DashboardSummaryProjection p = postRepository.summaryByAuthor(authorId, preRange.start(), preRange.end());
            return mapSummary(p, preRange.label());
        });

        // 4. Chờ cả 3 luồng hoàn thành (Non-blocking wait until all done)
        CompletableFuture.allOf(chartFuture, currentSummaryFuture, previousSummaryFuture).join();

        // 5. Gom kết quả trả về
        try {
            return DashboardResponse.builder()
                    .current(currentSummaryFuture.get())
                    .previous(previousSummaryFuture.get())
                    .chart(chartFuture.get())
                    .build();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Parallel execution failed", e);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    /* ==================== HELPER METHODS ==================== */

    private List<DashboardResponse.Chart> getChartData(Long authorId, String mode, DateRange range) {
        // Khởi tạo Map rỗng để fill gap (tránh việc DB trả về thiếu giờ/ngày)
        Map<String, DashboardResponse.Chart> map = new LinkedHashMap<>();

        if ("DAY".equals(mode)) {
            for (int h = 0; h < 24; h++) {
                String k = String.format("%02d", h);
                map.put(k, createEmptyChart(k));
            }
            fillDataToMap(map, postRepository.chartDayByAuthor(authorId, range.start(), range.end()));

        } else if ("MONTH".equals(mode)) {
            LocalDate d = range.start().toLocalDate();
            LocalDate e = range.end().toLocalDate();
            while (!d.isAfter(e)) {
                String k = d.toString();
                map.put(k, createEmptyChart(k));
                d = d.plusDays(1);
            }
            fillDataToMap(map, postRepository.chartMonthByAuthor(authorId, range.start(), range.end()));

        } else if ("YEAR".equals(mode)) {
            int y = range.start().getYear();
            for (int m = 1; m <= 12; m++) {
                String k = String.format("%04d-%02d", y, m);
                map.put(k, createEmptyChart(k));
            }
            fillDataToMap(map, postRepository.chartYearByAuthor(authorId, range.start(), range.end()));
        }

        return new ArrayList<>(map.values());
    }

    private DashboardResponse.Chart createEmptyChart(String time) {
        return DashboardResponse.Chart.builder()
                .time(time)
                .donateCount(0L).totalDonated(0.0)
                .totalView(0L).totalLike(0L).totalComment(0L)
                .build();
    }

    // Map dữ liệu từ DB (List<Map>) vào Chart Map
    private void fillDataToMap(Map<String, DashboardResponse.Chart> map, List<Map<String, Object>> rawData) {
        for (Map<String, Object> r : rawData) {
            String time = (String) r.get("time");
            if (time != null && map.containsKey(time)) {
                DashboardResponse.Chart c = map.get(time);
                // Cập nhật giá trị (Parse an toàn)
                c.setDonateCount(safeLong(r.get("donateCount")));
                c.setTotalDonated(safeDouble(r.get("totalDonated")));
                c.setTotalView(safeLong(r.get("totalView")));
                c.setTotalLike(safeLong(r.get("totalLike")));
                c.setTotalComment(safeLong(r.get("totalComment")));
            }
        }
    }

    private DashboardResponse.Summary mapSummary(DashboardSummaryProjection p, String time) {
        if (p == null) {
            return DashboardResponse.Summary.builder()
                    .time(time).totalPost(0L).donateCount(0L).totalDonated(0.0)
                    .totalView(0L).totalLike(0L).totalComment(0L).build();
        }
        return DashboardResponse.Summary.builder()
                .time(time)
                .totalPost(p.getTotalPost() != null ? p.getTotalPost() : 0L)
                .donateCount(p.getDonateCount() != null ? p.getDonateCount() : 0L)
                .totalDonated(p.getTotalDonated() != null ? p.getTotalDonated() : 0.0)
                .totalView(p.getTotalView() != null ? p.getTotalView() : 0L)
                .totalLike(p.getTotalLike() != null ? p.getTotalLike() : 0L)
                .totalComment(p.getTotalComment() != null ? p.getTotalComment() : 0L)
                .build();
    }

    private Long safeLong(Object o) {
        return o instanceof Number ? ((Number) o).longValue() : 0L;
    }

    private Double safeDouble(Object o) {
        return o instanceof Number ? ((Number) o).doubleValue() : 0.0;
    }
}