package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.request.SearchListPostRequest;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.dto.response.UserResponse;
import com.giveitup.giveitup_be.service.PostService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostController {
    PostService postService;

    @PostMapping(
            path = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResponse<PostResponse> createPost(
            @ModelAttribute PostRequest request,
            @Parameter(
                    description = "Danh sách ảnh bài viết",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                    )
            )
            @RequestParam(value = "files", required = false)  MultipartFile[]  files,
            @Parameter(description = "Chỉ số ảnh thumbnail (0-based)")
            @RequestParam(value = "thumbIndex", required = false) boolean thumbIndex
    ) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request, files, thumbIndex))
                .build();
    }

    @PutMapping("/{postId}")
    ApiResponse<PostResponse> updatePost(
            @PathVariable Long postId,
            @ModelAttribute PostRequest request,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "thumbIndex", required = false) boolean thumbIndex
    ) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.updatePost(postId,request, files, thumbIndex))
                .build();
    }
    @DeleteMapping("/{postId}")
    ApiResponse<String> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.<String>builder()
                .result("Post has been deleted")
                .build();
    }
    @GetMapping("/user/{userId}")
  ApiResponse<PagingResponse<PostResponse>> getPostByUser(
            @PathVariable Long userId,
            SearchListPostRequest request
    ) {
        Page<PostResponse> page = postService.getPostByUserId(userId,request);

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
    ApiResponse<PagingResponse<PostResponse>> getPosts(SearchListPostRequest request) {
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
    @GetMapping("/{postId}")
    ApiResponse<PostResponse> getPostById(@PathVariable Long postId) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getPostById(postId))
                .build();
    }
}
