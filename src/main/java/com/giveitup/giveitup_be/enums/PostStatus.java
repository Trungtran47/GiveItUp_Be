package com.giveitup.giveitup_be.enums;

public enum PostStatus {
    PENDING(10L, "Chờ duyệt"),      // chờ duyệt
    ACTIVE(20L, "Đang hoạt động"),  // đang hoạt động
    INACTIVE(30L, "Hết hạn"),      // tạm dừng hoặc ẩn
    COMPlETE(50L, "Hoàng thành"),
    REJECTED(90L, "Từ chối duyệt"), // Từ chối ngay từ đầu (khi còn Pending)
    BLOCKED(91L, "Đã bị chặn"); // từ chối, ẩn

    private final Long code;
    private final String label; // thêm label

    // constructor nhận code + label
    PostStatus(Long code, String label) {
        this.code = code;
        this.label = label;
    }
    public static PostStatus fromCode(Long code) {
        for (PostStatus status : PostStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        // Nếu không tìm thấy code hợp lệ thì bắn lỗi
        throw new IllegalArgumentException("Trạng thái không hợp lệ: " + code);
        // Hoặc dùng custom exception của bạn:
        // throw new AppException(ErrorCode.INVALID_STATUS);
    }
    public Long getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
