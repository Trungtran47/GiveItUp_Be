package com.giveitup.giveitup_be.enums;

public enum UserStatus {
    PENDING(10L),      // Chờ xác nhận
    USER(20L),        // Người dùng bình thường
    AUTHOR(30L),      // Tài khoản author
    INACTIVE(90L);    // Ngừng hoạt động

    private final Long code;

    UserStatus(Long code) {
        this.code = code;
    }

    public Long getCode() {
        return code;
    }
}
