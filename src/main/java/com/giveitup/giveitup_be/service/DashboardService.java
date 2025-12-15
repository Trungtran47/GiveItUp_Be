package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.response.DashboardResponse;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.projection.DashboardSummaryProjection;
import com.giveitup.giveitup_be.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PostRepository postRepository;
    private final UserService userService;

    @PreAuthorize("hasRole('AUTHOR')")
    public DashboardResponse dashboardAuthor(
            String mode,
            LocalDate date,
            Integer year,
            Integer month
    ) {
        if (mode == null) throw new AppException(ErrorCode.INVALID_REQUEST);
        mode = mode.toUpperCase();

        UserEntity user = userService.getMyInfoReturnEntity();
        Long authorId = user.getOrganization().getId();

        LocalDateTime curStart, curEnd, preStart, preEnd;
        String currentLabel, previousLabel;
        List<DashboardResponse.Chart> chartList;

        switch (mode) {
            /* ================= DAY ================= */
            case "DAY" -> {
                if (date == null) throw new AppException(ErrorCode.INVALID_REQUEST);

                LocalDate cur = date;
                LocalDate pre = date.minusDays(1);

                curStart = cur.atStartOfDay();
                curEnd = cur.atTime(23, 59, 59);
                preStart = pre.atStartOfDay();
                preEnd = pre.atTime(23, 59, 59);

                currentLabel = cur.toString();
                previousLabel = pre.toString();

                chartList = fillChartDay(authorId, curStart, curEnd);
            }

            /* ================= MONTH ================= */
            case "MONTH" -> {
                if (year == null || month == null) throw new AppException(ErrorCode.INVALID_REQUEST);

                YearMonth cur = YearMonth.of(year, month);
                YearMonth pre = cur.minusMonths(1);

                curStart = cur.atDay(1).atStartOfDay();
                curEnd = cur.atEndOfMonth().atTime(23, 59, 59);
                preStart = pre.atDay(1).atStartOfDay();
                preEnd = pre.atEndOfMonth().atTime(23, 59, 59);

                currentLabel = cur.toString();
                previousLabel = pre.toString();

                chartList = fillChartMonth(authorId, curStart, curEnd);
            }

            /* ================= YEAR ================= */
            case "YEAR" -> {
                if (year == null) throw new AppException(ErrorCode.INVALID_REQUEST);

                curStart = LocalDate.of(year, 1, 1).atStartOfDay();
                curEnd = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
                preStart = curStart.minusYears(1);
                preEnd = curEnd.minusYears(1);

                currentLabel = String.valueOf(year);
                previousLabel = String.valueOf(year - 1);

                chartList = fillChartYear(authorId, curStart, curEnd);
            }

            default -> throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        DashboardSummaryProjection currentSummary = postRepository.summaryByAuthor(authorId, curStart, curEnd);
        DashboardSummaryProjection previousSummary = postRepository.summaryByAuthor(authorId, preStart, preEnd);

        return DashboardResponse.builder()
                .current(mapSummary(currentSummary, currentLabel))
                .previous(mapSummary(previousSummary, previousLabel))
                .chart(chartList)
                .build();
    }

    /* ==================== HELPERS ==================== */
    private DashboardResponse.Summary mapSummary(DashboardSummaryProjection p, String time) {
        return DashboardResponse.Summary.builder()
                .time(time)
                .totalPost(p.getTotalPost())
                .donateCount(p.getDonateCount())
                .totalDonated(p.getTotalDonated())
                .totalView(p.getTotalView())
                .totalLike(p.getTotalLike())
                .totalComment(p.getTotalComment())
                .build();
    }

    private List<DashboardResponse.Chart> fillChartDay(Long authorId, LocalDateTime start, LocalDateTime end) {
        Map<String, DashboardResponse.Chart> map = new LinkedHashMap<>();
        for (int h = 0; h < 24; h++) {
            String hour = String.format("%02d", h);
            map.put(hour, DashboardResponse.Chart.builder()
                    .time(hour)
                    .donateCount(0L)
                    .totalDonated(0.0)
                    .totalView(0L)
                    .totalLike(0L)
                    .totalComment(0L)
                    .build());
        }

        List<Map<String, Object>> raw = postRepository.chartDayByAuthor(authorId, start, end);
        for (Map<String, Object> r : raw) {
            String hour = (String) r.get("time");
            map.put(hour, DashboardResponse.Chart.builder()
                    .time(hour)
                    .donateCount(((Number) r.get("donateCount")).longValue())
                    .totalDonated(((Number) r.get("totalDonated")).doubleValue())
                    .totalView(((Number) r.get("totalView")).longValue())
                    .totalLike(((Number) r.get("totalLike")).longValue())
                    .totalComment(((Number) r.get("totalComment")).longValue())
                    .build());
        }
        return new ArrayList<>(map.values());
    }

    private List<DashboardResponse.Chart> fillChartMonth(Long authorId, LocalDateTime start, LocalDateTime end) {
        Map<String, DashboardResponse.Chart> map = new LinkedHashMap<>();
        LocalDate d = start.toLocalDate();
        while (!d.isAfter(end.toLocalDate())) {
            String day = d.toString();
            map.put(day, DashboardResponse.Chart.builder()
                    .time(day)
                    .donateCount(0L)
                    .totalDonated(0.0)
                    .totalView(0L)
                    .totalLike(0L)
                    .totalComment(0L)
                    .build());
            d = d.plusDays(1);
        }

        List<Map<String, Object>> raw = postRepository.chartMonthByAuthor(authorId, start, end);
        for (Map<String, Object> r : raw) {
            String day = (String) r.get("time");
            map.put(day, DashboardResponse.Chart.builder()
                    .time(day)
                    .donateCount(((Number) r.get("donateCount")).longValue())
                    .totalDonated(((Number) r.get("totalDonated")).doubleValue())
                    .totalView(((Number) r.get("totalView")).longValue())
                    .totalLike(((Number) r.get("totalLike")).longValue())
                    .totalComment(((Number) r.get("totalComment")).longValue())
                    .build());
        }
        return new ArrayList<>(map.values());
    }

    private List<DashboardResponse.Chart> fillChartYear(Long authorId, LocalDateTime start, LocalDateTime end) {
        Map<String, DashboardResponse.Chart> map = new LinkedHashMap<>();
        int year = start.getYear();
        for (int m = 1; m <= 12; m++) {
            String monthStr = String.format("%04d-%02d", year, m);
            map.put(monthStr, DashboardResponse.Chart.builder()
                    .time(monthStr)
                    .donateCount(0L)
                    .totalDonated(0.0)
                    .totalView(0L)
                    .totalLike(0L)
                    .totalComment(0L)
                    .build());
        }

        List<Map<String, Object>> raw = postRepository.chartYearByAuthor(authorId, start, end);
        for (Map<String, Object> r : raw) {
            String monthStr = (String) r.get("time");
            map.put(monthStr, DashboardResponse.Chart.builder()
                    .time(monthStr)
                    .donateCount(((Number) r.get("donateCount")).longValue())
                    .totalDonated(((Number) r.get("totalDonated")).doubleValue())
                    .totalView(((Number) r.get("totalView")).longValue())
                    .totalLike(((Number) r.get("totalLike")).longValue())
                    .totalComment(((Number) r.get("totalComment")).longValue())
                    .build());
        }
        return new ArrayList<>(map.values());
    }

}
