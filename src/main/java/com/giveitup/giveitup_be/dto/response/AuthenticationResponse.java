package com.giveitup.giveitup_be.dto.response;

import com.giveitup.giveitup_be.entity.RoleEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
//    String token;
//    boolean authenticated;
    String token;          // Access token
    long expires;          // Thời gian hết hạn (tính bằng mili giây)
    boolean authenticated; // true nếu đăng nhập thành công
//    RoleResponse role;
}
