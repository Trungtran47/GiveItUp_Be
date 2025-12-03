package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.response.LikeResponse;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.service.LikeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/like")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LikeController {
    LikeService likeService;

    @PostMapping("/toggleLike/{postId}")
    public ApiResponse<LikeResponse> toggleLike(@PathVariable Long postId) {
        return ApiResponse.<LikeResponse>builder()
                .result(likeService.toggleLike(postId))
                .build();
    }
    @GetMapping("/user/{userId}/likedPosts")
    public ApiResponse<PagingResponse<PostResponse>> getPostByUserId(@PathVariable Long userId,@ModelAttribute BasePagingRequest request) {
        Page<PostResponse> page = likeService.getPostsByUserId(userId, request);
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
}
