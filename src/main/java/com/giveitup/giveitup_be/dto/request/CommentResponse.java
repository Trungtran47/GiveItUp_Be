package com.giveitup.giveitup_be.dto.request;


import com.giveitup.giveitup_be.dto.response.UserResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    private Long id;
    private String content;
    private Long userId;
    private String avatar;
    private String userName;
    private Long postId;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies;  // Danh sách reply

    Long likeCount;
    Long dislikeCount;
    String myReaction;
}
