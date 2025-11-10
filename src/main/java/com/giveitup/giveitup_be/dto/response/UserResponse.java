package com.giveitup.giveitup_be.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
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
    String gender;
    String email;
    Long phoneNumber;
    RoleResponse roles;
}
