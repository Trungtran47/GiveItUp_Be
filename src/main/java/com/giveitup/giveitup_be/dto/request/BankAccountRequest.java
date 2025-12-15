package com.giveitup.giveitup_be.dto.request;

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
public class BankAccountRequest {
    Long id;
    String bankAccountNumber;  // Số tài khoản
    String accountCode;           // Mã tài khoản
    String accountHolderName;   // Chủ tài khoản
    String bankName;
    Long organization;
    Long status;

}
