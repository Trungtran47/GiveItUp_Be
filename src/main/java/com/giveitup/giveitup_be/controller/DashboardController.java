package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.response.DashboardAdminResponse;
import com.giveitup.giveitup_be.dto.response.DashboardResponse;
import com.giveitup.giveitup_be.service.DashboardAdminService;
import com.giveitup.giveitup_be.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final DashboardAdminService dashboardAdminService;

    @GetMapping("/author")
    public ApiResponse<DashboardResponse> dashboardAuthor(
            @RequestParam String mode,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.<DashboardResponse>builder()
                .result(dashboardService.dashboardAuthor(mode, date, year, month))
                .build();
    }

    @GetMapping("/admin")
    public ApiResponse<DashboardAdminResponse> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        if (fromDate == null) fromDate = LocalDate.now().withDayOfMonth(1);
        if (toDate == null) toDate = LocalDate.now();
        DashboardAdminResponse response = dashboardAdminService.getDashboardData(fromDate, toDate);
        return ApiResponse.<DashboardAdminResponse>builder()
                .result(response)
                .build();
    }
}
