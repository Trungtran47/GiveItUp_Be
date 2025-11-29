package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DonateResponse {
    Long id;
    Long paymentCode;
    Double amount;
//    boolean isShow;
    String description;
    LocalDateTime donatedAt;
    UserPostResponse user;
    PostResponse post;

}