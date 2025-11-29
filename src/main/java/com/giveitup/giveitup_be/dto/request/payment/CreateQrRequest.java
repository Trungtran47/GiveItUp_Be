package com.giveitup.giveitup_be.dto.request.payment;

import lombok.Data;

@Data
public class CreateQrRequest {
    private Long orderCode;
    private long amount;
    private String description;


}
