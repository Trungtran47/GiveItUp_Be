package com.giveitup.giveitup_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProcessPayoutRequest {
    Long payoutId;
    boolean approved;      // true = approve, false = reject
    Double adminTransferAmount;
    String noteAdmin;           // ghi chú
    MultipartFile transferProofImage;
    String transferProofImageUrl;
}
