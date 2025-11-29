package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.DonateRequest;
import com.giveitup.giveitup_be.dto.request.SearchListDonateRequest;
import com.giveitup.giveitup_be.dto.response.DonateResponse;
import com.giveitup.giveitup_be.dto.response.DonateSummary;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.service.DonateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/donate")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DonateController {
    DonateService donateService;
    @PostMapping("/create")
    ApiResponse<DonateResponse> createDonate(@RequestBody DonateRequest request) {
        return ApiResponse.<DonateResponse>builder()
                .result(donateService.createDonate(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<PagingResponse<DonateResponse>> getAllDonates(@ModelAttribute SearchListDonateRequest request) {
        Page<DonateResponse> page = donateService.getDonate(request);
        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();
        PagingResponse<DonateResponse> pagingResponse = PagingResponse.<DonateResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<DonateResponse>>builder()
                .result(pagingResponse)
                .build();
    }

    @GetMapping("/total_amount/{postId}")
    ApiResponse<PagingResponse<DonateSummary>> getDonateTotalAmount(@PathVariable Long postId, @ModelAttribute SearchListDonateRequest request) {
        Page<DonateSummary> page = donateService.getDonateTotalAmount(postId,request);
        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();
        PagingResponse<DonateSummary> pagingResponse = PagingResponse.<DonateSummary>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<DonateSummary>>builder()
                .result(pagingResponse)
                .build();
    }
}
