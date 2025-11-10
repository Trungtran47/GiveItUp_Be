package com.giveitup.giveitup_be.dto.request;

import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostRequest {
    String title;
    String description;
    Double targetAmount;
    LocalDateTime startDate;
    LocalDateTime endDate;
    Long status;
    Long userId;
    Long categoryId;
}
