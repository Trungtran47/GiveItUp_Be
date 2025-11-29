package com.giveitup.giveitup_be.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QRRequest {
    private String accountNo;     // Số tài khoản
    private String accountName;   // Tên chủ tài khoản
    private String acqId;         // Mã ngân hàng (ví dụ: 970415)
    private String addInfo;       // Nội dung chuyển khoản
    private String amount;        // Số tiền
    private String template;      // "compact" hoặc "full"
}
