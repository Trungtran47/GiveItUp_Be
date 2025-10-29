package com.giveitup.giveitup_be.enums;

public enum CategoryStatus {
    ACTIVE(10),     // đang hoạt động
    INACTIVE(20),   // tạm dừng hoặc ẩn
    DELETED(30);    // đã xoá

    private final int code;

    CategoryStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
