package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.CommentRequest;
import com.giveitup.giveitup_be.dto.request.CommentResponse;
import com.giveitup.giveitup_be.dto.response.CommentResponseForUser;
import com.giveitup.giveitup_be.enums.ReactionType;
import com.giveitup.giveitup_be.service.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comment")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CommentController {
    CommentService commentService;

    @PostMapping("/create/{userId}")
    public ApiResponse<CommentResponse> createComment(@PathVariable Long userId, @RequestBody CommentRequest request) {
        return ApiResponse.<CommentResponse>builder()
                .result(commentService.createComment(userId, request))
                .build();
    }
    @GetMapping("/get/{postId}")
    public ApiResponse<List<CommentResponse>> getCommentsByPost(@PathVariable Long postId) {
        return ApiResponse.<List<CommentResponse> >builder()
                .result(commentService.getCommentsByPost(postId))
                .build();
    }
    @GetMapping("/get/my_comment")
    public ApiResponse<List<CommentResponseForUser>> getCommentsByUserId() {
        return ApiResponse.<List<CommentResponseForUser> >builder()
                .result(commentService.getCommentsByUserId())
                .build();
    }
    @DeleteMapping("/{userId}/delete/{commentId}")
    public ApiResponse<String> deleteComment(@PathVariable Long commentId, @PathVariable Long userId) {
        commentService.deleteComment(userId,commentId);
        return ApiResponse.<String>builder()
                .result("Comment has been deleted")
                .build();
    }
    // API: POST /api/comments/{id}/reaction?type=LIKE
    @PostMapping("/{commentId}/reaction")
    public ApiResponse<String> reactToComment(
            @PathVariable Long commentId,
            @RequestParam ReactionType type) {

        commentService.reactToComment(commentId, type);
        return ApiResponse.<String>builder()
                .result("Success")
                .build();
    }
}
