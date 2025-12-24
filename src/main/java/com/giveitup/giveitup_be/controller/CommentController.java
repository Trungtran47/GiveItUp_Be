package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.CommentRequest;
import com.giveitup.giveitup_be.dto.request.CommentResponse;
import com.giveitup.giveitup_be.dto.response.CommentResponseForUser;
import com.giveitup.giveitup_be.dto.response.FlaskCommentResponse;
import com.giveitup.giveitup_be.enums.ReactionType;
import com.giveitup.giveitup_be.service.AiService;
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
    AiService aiService;

    @PostMapping("/create/{userId}")
    public ApiResponse<Object> createComment(@PathVariable Long userId, @RequestBody CommentRequest request) {
        // 1. Gọi AI service
        FlaskCommentResponse aiResponse = aiService.checkComment(request.getContent());
        // 2. Trường hợp 1: Lỗi kết nối đến AI (aiResponse null)
        if (aiResponse == null) {
            return ApiResponse.builder()
                    .code(500) // Mã lỗi server
                    .message("Lỗi kết nối đến AI server") // Thường dùng field message cho lỗi
                    .build();
        }
        // 3. Trường hợp 2: AI phát hiện tiêu cực (Negative/Alert)
        if (aiResponse.isAlert() || "negative".equals(aiResponse.getLabel())) {
            return ApiResponse.builder()
                    .code(400) // Mã lỗi bad request
                    .message("Bình luận của bạn vi phạm tiêu chuẩn cộng đồng.")
                    .build();
        }
        // 4. Trường hợp 3: Hợp lệ -> Gọi Service lưu vào DB
        return ApiResponse.builder()
                .code(200) // Thành công
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
