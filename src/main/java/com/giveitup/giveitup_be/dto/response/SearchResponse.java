package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchResponse {
     List<PostSearchResponse> posts;
     List<UserSearchResponse> users;
    @Data
    public static class PostSearchResponse {
        Long id;
        String title;
        String image;
        Double targetAmount;
        Double donatedAmount;
    }

    @Data
    public static class UserSearchResponse {
        Long id;
        String fullName;
        String imageUser;
        String organizationName;
    }
    @Data
    public static class HistoryResponse {
        Long id;
        String keyword;
        LocalDateTime createdAt;
    }
}
