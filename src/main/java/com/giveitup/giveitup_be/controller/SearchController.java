package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.response.SearchResponse;
import com.giveitup.giveitup_be.service.SearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<SearchResponse> search(@RequestParam String keyword) {
        return ApiResponse.<SearchResponse>builder()
                .result(searchService.search(keyword))
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
