package com.giveitup.giveitup_be.dto.request;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchListUserRequest extends BasePagingRequest {
    String userName;
    String PhoneNumber;

    // Thêm trường tìm kiếm tên tổ chức (nếu cần tách riêng)
    private String organizationName;

    // Thêm trường này để Controller quyết định lấy list USER hay AUTHOR
    private String role; // Giá trị: "USER" hoặc "AUTHOR"
}
