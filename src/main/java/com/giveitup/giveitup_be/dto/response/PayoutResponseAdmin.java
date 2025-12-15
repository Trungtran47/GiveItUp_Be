package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayoutResponseAdmin {
    Long id;
    PostResponse post;
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
    OrganizationResponse requestedBy;
//    UserResponse approvedBy;
    UserResponse createdByAdmin;
}
