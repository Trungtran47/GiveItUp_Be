package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.*;
import com.giveitup.giveitup_be.dto.response.PayoutResponse;
import com.giveitup.giveitup_be.dto.response.PayoutResponseAdmin;
import com.giveitup.giveitup_be.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;

    // -----------------------------------------
    // 1. AUTHOR REQUEST PAYOUT
    // -----------------------------------------
    @PostMapping("/request")
    public ApiResponse<PayoutResponse> authorRequest(
            @RequestBody CreatePayoutRequest req
    ) {
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.authorRequestPayout(req))
                .build();
    }
    @PutMapping("/update")
    public ApiResponse<PayoutResponse> authorUpdatePayout(
            @RequestBody UpdatePayoutRequest req
    ) {
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.updatePayout(req))
                .build();
    }

    @DeleteMapping("/{authorId}/delete/{payoutId}")
    public ApiResponse<String> deletePayout(
            @PathVariable Long payoutId,
            @PathVariable Long authorId
    ) {
        payoutService.deletePayout(payoutId, authorId);
        return ApiResponse.<String>builder()
                .result("Request payout has been deleted")
                .build();
    }

    // -----------------------------------------
    // 2. ADMIN APPROVE OR REJECT (DÙNG CHUNG API)
    // -----------------------------------------
    @PutMapping(value = "/process/{adminId}", consumes = {"multipart/form-data"})
    public ApiResponse<PayoutResponse> adminProcess(
            @ModelAttribute ProcessPayoutRequest req,
            @PathVariable Long adminId
    ) {
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.adminProcessPayout(req, adminId))
                .build();
    }

    // -----------------------------------------
    // 4. AUTHOR CONFIRM RECEIVED MONEY
    // -----------------------------------------
    @PutMapping("/{authorId}/confirm/{payoutId}")
    public ApiResponse<PayoutResponse> authorConfirm( @PathVariable Long authorId,
            @PathVariable Long payoutId
    ) {
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.authorConfirm(authorId, payoutId))
                .build();
    }

    // -----------------------------------------
    // 5. ADMIN CREATE PAYOUT DIRECTLY
    // -----------------------------------------
    @PostMapping("/admin-create/{adminId}")
    public ApiResponse<PayoutResponse> adminCreate(
            @RequestBody AdminCreatePayoutRequest req,
            @PathVariable Long adminId
    ) {
        return ApiResponse.<PayoutResponse>builder()
                .result(payoutService.adminCreatePayout(req, adminId))
                .build();
    }
    @GetMapping("/author/{OrganizationId}")
    ApiResponse<PagingResponse<PayoutResponse>> getPayoutsByUserId(
            @PathVariable Long OrganizationId,
            @ModelAttribute BasePagingRequest request
    ) {
        Page<PayoutResponse> page = payoutService.getPayoutsOrganizationId(OrganizationId,request);
        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<PayoutResponse> pagingResponse = PagingResponse.<PayoutResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<PayoutResponse>>builder()
                .result(pagingResponse)
                .build();
    }
    @GetMapping
    ApiResponse<PagingResponse<PayoutResponseAdmin>> getAllPayouts(
            @ModelAttribute BasePagingRequest request
    ) {
        Page<PayoutResponseAdmin> page = payoutService.getAllPayouts(request);
        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<PayoutResponseAdmin> pagingResponse = PagingResponse.<PayoutResponseAdmin>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<PayoutResponseAdmin>>builder()
                .result(pagingResponse)
                .build();
    }

}

