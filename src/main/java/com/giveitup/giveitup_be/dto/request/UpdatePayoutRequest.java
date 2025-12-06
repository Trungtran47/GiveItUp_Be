package com.giveitup.giveitup_be.dto.request;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePayoutRequest {
    private Long payoutId;
    private Long amount;
    private String note;
}
