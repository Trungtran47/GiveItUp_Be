package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.RoleRequest;
import com.giveitup.giveitup_be.dto.response.RoleResponse;
import com.giveitup.giveitup_be.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Role Management", description = "APIs for managing user roles")
public class RoleController {

    RoleService roleService;

    @Operation(summary = "Create new role", description = "Tạo mới một role trong hệ thống")
    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @Operation(summary = "Get all roles", description = "Lấy danh sách tất cả role")
    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @Operation(summary = "Delete role", description = "Xóa role theo tên role")
    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return ApiResponse.<Void>builder().build();
    }
}
