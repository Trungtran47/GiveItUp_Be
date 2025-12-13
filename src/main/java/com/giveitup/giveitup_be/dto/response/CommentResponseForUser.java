package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponseForUser {
    Long id;
    String content;
    PostResponse post;
    LocalDateTime createdAt;
}
