package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.PostUpdateRequest;
import com.giveitup.giveitup_be.dto.response.PostUpdateResponse;
import com.giveitup.giveitup_be.service.PostUpdateService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@RequestMapping("/post_update")
public class PostUpdateController {
    PostUpdateService postUpdateService;

    //    @PostMapping("/create/{userId}")
//    public ApiResponse<CommentResponse> createComment(@PathVariable Long userId, @RequestBody CommentRequest request) {
//        return ApiResponse.<CommentResponse>builder()
//                .result(commentService.createComment(userId, request))
//                .build();
//    }
    @PostMapping(value="/create/{userId}", consumes = {"multipart/form-data"})
    public ApiResponse<PostUpdateResponse> create(@PathVariable Long userId, @ModelAttribute PostUpdateRequest request) {
        return  ApiResponse.<PostUpdateResponse>builder()
                .result(postUpdateService.createPostUpdate(userId,request))
                .build();
    }
    @PutMapping(value = "{userId}/create/{postUpdateId}", consumes = {"multipart/form-data"})
    public ApiResponse<PostUpdateResponse> update(@PathVariable Long userId,@PathVariable Long postUpdateId, @ModelAttribute PostUpdateRequest request) {
        return  ApiResponse.<PostUpdateResponse>builder()
                .result(postUpdateService.updatePostUpdate(userId,postUpdateId,request))
                .build();
    }
}
