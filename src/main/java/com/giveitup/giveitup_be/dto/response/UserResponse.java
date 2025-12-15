package com.giveitup.giveitup_be.dto.response;

import com.giveitup.giveitup_be.enums.UserStatus;
import jakarta.persistence.Column;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long id;
    String username;
    String firstName;
    String lastName;
    LocalDate dob;
    Long gender;
    String email;
    String phoneNumber;
    String role;
    String address;
    String imageUser;
    String publicImageUserId;
    Boolean isPublic ;
    String introduce;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Long status;
    OrganizationResponse organization;

    Long totalFollowers;
    Long totalFollowing;
    Boolean isFollowing;
}
