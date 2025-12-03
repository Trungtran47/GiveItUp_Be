package com.giveitup.giveitup_be.dto.request;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchListDonateRequest extends BasePagingRequest {
    private Long userId;
}
