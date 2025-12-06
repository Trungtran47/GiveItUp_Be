package com.giveitup.giveitup_be.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum PayoutStatus {
    PENDING(10L, "Đang chờ duyệt"),
    TRANSFERRED(20L, "Đã chuyển khoản"),
    AUTHOR_CONFIRMED(30L, "Tác giả đã xác nhận"),
    REJECTED(40L, "Đã từ chối");

    private final Long code;
    private final String label;

    PayoutStatus(Long code, String label) {
        this.code = code;
        this.label = label;
    }

    public static PayoutStatus fromCode(Long code) {
        return Arrays.stream(PayoutStatus.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
