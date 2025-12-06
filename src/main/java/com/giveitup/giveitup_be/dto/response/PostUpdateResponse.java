package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostUpdateResponse {
    Long id;
    String content;
    String imagePostUpdateUrl;
    String imagePostUpdatePublicId;
    Long postId;
    Long payoutId;
    LocalDateTime createdAt;
}
