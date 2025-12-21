package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.request.ReviewPostRequest;
import com.giveitup.giveitup_be.dto.request.SearchListPostRequest;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.PostMapResponse;
import com.giveitup.giveitup_be.service.PostService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostController {
    PostService postService;
    @GetMapping("/top5")
    public ApiResponse<List<PostResponse>> getTop5Post() {
        List<PostResponse> result = postService.getTop5();
        return ApiResponse.<List<PostResponse>>builder()
                .result(result)
                .build();
    }
    @GetMapping("/map")
    public ApiResponse<List<PostMapResponse>> getPostsOnMap(@RequestParam String city) {
        List<PostMapResponse> result = postService.getPostsByCity(city);
        return ApiResponse.<List<PostMapResponse>>builder()
                .result(result)
                .build();
    }
    @PostMapping(path = "/create", consumes = {"multipart/form-data"})
    public ApiResponse<PostResponse> createPost(@ModelAttribute PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request))
                .build();
    }

    @PutMapping(path ="/update/{postId}", consumes = {"multipart/form-data"})
    public ApiResponse<PostResponse> updatePost(@PathVariable Long postId, @ModelAttribute PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.updatePost(postId,request))
                .build();
    }
    @DeleteMapping("/delete/{postId}")
    ApiResponse<String> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.<String>builder()
                .result("Post has been deleted")
                .build();
    }
    @GetMapping("/organization/{OrganizationId}")
    ApiResponse<PagingResponse<PostResponse>> getPostByOrganizationId(@PathVariable Long OrganizationId, SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPostByOrganizationId(OrganizationId,request);
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
    @GetMapping
    ApiResponse<PagingResponse<PostResponse>> getPosts(@ModelAttribute SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPosts(request);

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
    @GetMapping("/admin")
    ApiResponse<PagingResponse<PostResponse>> getPostsAdmin(@ModelAttribute SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPostsAdmin(request);

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
    @GetMapping("/categoryId/{categoryId}")
    ApiResponse<PagingResponse<PostResponse>> getPostsByCategoryId(@PathVariable Long categoryId,@ModelAttribute SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPostsByCategory(categoryId, request);

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
    @GetMapping("/{postId}")
    ApiResponse<PostResponse> getPostById(@PathVariable Long postId) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getPostById(postId))
                .build();
    }
    @PutMapping("/update-status/{postId}")
    public ApiResponse<String> reviewPost(@PathVariable Long postId,
                                          @RequestBody ReviewPostRequest request) {
        postService.reviewPost(postId, request);
        return ApiResponse.<String>builder()
                .result("Trạng thái đã được cập nhật")
                .build();
    }
}
