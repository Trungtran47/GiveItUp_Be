package com.giveitup.giveitup_be.dto.request;

import com.giveitup.giveitup_be.validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @Size(min = 4, message = "USERNAME_INVALID")
    String username;

    @Size(min = 6, message = "INVALID_PASSWORD")
    String password;

    String firstName;
    String lastName;
    @Email(message = "INVALID_EMAIL")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "EMAIL_MUST_BE_GMAIL")
    String email;
    @Size(max = 10, message = "INVALID_PHONE_NUMBER")
    String phoneNumber;
    Long gender;
//    String role;

    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;
}
