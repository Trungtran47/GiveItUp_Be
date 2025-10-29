package com.giveitup.giveitup_be.dto.paging;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BasePagingResponse<T> {
    private PagingInfo paging;
    private List<T> Data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PagingInfo {
        int currentPage;
        int numberOfRecord;
        long totalRecord;
        int totalPages;
    }
}
