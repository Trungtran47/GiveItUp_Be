package com.giveitup.giveitup_be.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentRequest {
     String content;         // Nội dung comment
     Long postId;            // ID bài post
     Long parentCommentId;   // Nếu là reply, ID comment cha
}
