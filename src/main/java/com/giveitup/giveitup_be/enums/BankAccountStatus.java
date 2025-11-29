package com.giveitup.giveitup_be.enums;

public enum BankAccountStatus {
    ACTIVE(10L),     // đang hoạt động
    INACTIVE(20L),   // tạm dừng hoặc ẩn
    DELETED(30L);    // đã xoá    // Ngừng hoạt động

    private final Long code;

    BankAccountStatus(Long code) {
        this.code = code;
    }

    public Long getCode() {
        return code;
    }
}
