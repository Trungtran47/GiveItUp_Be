package com.giveitup.giveitup_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePayoutRequest {
    Long postId;
    Double amount;
    String note;
}
