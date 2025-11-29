package com.giveitup.giveitup_be.dto.request;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchListBankAccountRequest extends BasePagingRequest {
    String bankAccountNumber;
    String accountCode;
    Long status;
}
