package com.giveitup.giveitup_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@Builder
public class PostAIResponse {
    private Long postId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}
