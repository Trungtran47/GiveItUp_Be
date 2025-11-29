package com.giveitup.giveitup_be.enums;

public enum PostStatus {
    PENDING(10L, "Chờ duyệt"),      // chờ duyệt
    ACTIVE(20L, "Đang hoạt động"),  // đang hoạt động
    INACTIVE(30L, "Tạm dừng"),      // tạm dừng hoặc ẩn
    REJECTED(90L, "Không được duyệt"); // từ chối

    private final Long code;
    private final String label; // thêm label

    // constructor nhận code + label
    PostStatus(Long code, String label) {
        this.code = code;
        this.label = label;
    }

    public Long getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
