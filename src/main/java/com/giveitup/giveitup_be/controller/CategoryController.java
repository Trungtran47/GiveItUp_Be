package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.CategoryRequest;
import com.giveitup.giveitup_be.dto.request.SearchListCategoryRequest;
import com.giveitup.giveitup_be.dto.request.SearchListUserRequest;
import com.giveitup.giveitup_be.dto.response.CategoryResponse;
import com.giveitup.giveitup_be.dto.response.UserResponse;
import com.giveitup.giveitup_be.service.CategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }
    @PutMapping("/{categoryId}")
    ApiResponse<CategoryResponse> updateCategory(@Valid @PathVariable long categoryId, @RequestBody CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.updateCategory(categoryId, request))
                .build();
    }
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponse.<Void>builder()
                .build();
    }

    @GetMapping
    public ApiResponse<PagingResponse<CategoryResponse>> getCategories(@ModelAttribute SearchListCategoryRequest request) {
        Page<CategoryResponse> page = categoryService.getCategories(request);
        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();
        PagingResponse<CategoryResponse> pagingResponse = PagingResponse.<CategoryResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();

        return ApiResponse.<PagingResponse<CategoryResponse>>builder()
                .result(pagingResponse)
                .build();
    }
    @GetMapping("/{categoryId}")
    ApiResponse<CategoryResponse> getUser(@PathVariable("categoryId") Long categoryId) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(categoryId))
                .build();
    }
}
