package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayoutResponse {
    Long id;
    Long postId;
    Double amount;
    Double adminTransferAmount;
    Long status;
    String statusName;
    String type;
    String note;
    String noteAdmin;
    String transferProofImageUrl;
    String transferProofImagePublicId;
    LocalDateTime requestedAt;
//    LocalDateTime approvedAt;
    LocalDateTime confirmedAt;
    LocalDateTime createdByAdminAt;


    Long requestedBy;
//    Long approvedBy;
    Long createdByAdmin;
    PostUpdateResponse postUpdate;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
