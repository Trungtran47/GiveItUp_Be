package com.giveitup.giveitup_be.dto.request.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayOSWebhookBodyPayload {

    private Map<String, Object> data; // Dữ liệu order, ví dụ orderCode, status, amount
    private String signature; // Signature từ PayOS
}
