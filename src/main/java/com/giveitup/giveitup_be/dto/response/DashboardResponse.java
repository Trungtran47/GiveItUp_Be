package com.giveitup.giveitup_be.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private Summary current;
    private Summary previous;
    private List<Chart> chart;

    /* ================== INNER CLASS ================== */

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {

        private String time;

        private Long totalPost;
        private Long donateCount;
        private Double totalDonated;

        private Long totalView;
        private Long totalLike;
        private Long totalComment;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Chart {

        private String time;

        private Long donateCount;
        private Double totalDonated;

        private Long totalView;
        private Long totalLike;
        private Long totalComment;
    }
}
