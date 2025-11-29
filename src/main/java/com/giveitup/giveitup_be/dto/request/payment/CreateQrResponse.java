package com.giveitup.giveitup_be.dto.request.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateQrResponse {
    private String orderId;
    private long amount;
    private String qrData;
    private String payUrl;
    private String status;
    private String description;
}
