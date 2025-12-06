package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class FollowResponse {
    Long id;
    String fullName;
    String avatarUrl;
    String email;
}
