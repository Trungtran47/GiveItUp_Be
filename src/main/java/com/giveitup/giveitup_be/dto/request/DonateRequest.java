package com.giveitup.giveitup_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class  DonateRequest {
    Long paymentCode;
    Double amount;
//    boolean isShow;
    String description;
    Long userId;
    Long postId;

}
