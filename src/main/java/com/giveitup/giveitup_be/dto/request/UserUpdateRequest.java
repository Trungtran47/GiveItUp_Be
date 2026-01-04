package com.giveitup.giveitup_be.dto.request;

import com.giveitup.giveitup_be.validator.DobConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;
    String phoneNumber;
    Long gender;
    String address;
    boolean deleteImage;
    MultipartFile imageUser;
    String publicImageUserId;
    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

//    List<String> roles;
    String roles;
}
