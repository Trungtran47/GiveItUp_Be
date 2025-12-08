package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.dto.response.SearchResponse;
import com.giveitup.giveitup_be.service.SearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchController {

      SearchService searchService;

    @GetMapping
    public ApiResponse<PagingResponse<PostResponse>> search(@RequestParam(required = false) String keyword,@ModelAttribute BasePagingRequest request) {
        Page<PostResponse> page = searchService.search(keyword, request);

        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<PostResponse> pagingResponse = PagingResponse.<PostResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<PostResponse>>builder()
                .result(pagingResponse)
                .build();
    }
    @GetMapping("/history")
    public ApiResponse<List<SearchResponse.HistoryResponse>> getHistorySearch() {
        return ApiResponse.< List<SearchResponse.HistoryResponse>>builder()
                .result(searchService.getSearchHistory())
                .build();
    }
    @DeleteMapping("/history/{historyId}")
    public ApiResponse<String> deleteHistorySearch(@PathVariable Long historyId) {
        searchService.deleteSearchHistory(historyId);
        return ApiResponse.<String>builder()
                .result("deleted")
                .build();
    }
    @DeleteMapping("/history/all")
    public ApiResponse<String> deleteAllHistorySearch() {
        searchService.deleteAllSearchHistory();
        return ApiResponse.<String>builder()
                .result("deleted")
                .build();
    }


}
