package com.giveitup.giveitup_be.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchAIResponse {
    private Long userId;
    private String keyword;
    private LocalDateTime createdAt;
}