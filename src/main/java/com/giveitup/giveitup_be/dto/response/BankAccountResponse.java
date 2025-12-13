package com.giveitup.giveitup_be.dto.response;

import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BankAccountResponse {
    Long id;
    String bankAccountNumber;
    String accountCode;
    String accountHolderName;
    String bankName;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Long status;
    Long userId;

}
