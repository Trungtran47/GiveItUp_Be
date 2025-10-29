package com.giveitup.giveitup_be.dto.paging;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagingResponse<T> {

    @JsonProperty("Paging")
    private PagingInfo Paging;

    @JsonProperty("Data")
    private List<T> Data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class PagingInfo {

        @JsonProperty("CurrentPage")
        int CurrentPage;

        @JsonProperty("NumberOfRecord")
        int NumberOfRecord;

        @JsonProperty("TotalRecord")
        long TotalRecord;

        @JsonProperty("TotalPages")
        int TotalPages;
    }
}
