package com.giveitup.giveitup_be.dto.paging;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasePagingRequest {
    Integer CurrentPage = 1;
    Integer PageSize = 50;

//    public int getOffset() {
//        return (CurrentPage - 1) * PageSize;
//    }
}
