package com.giveitup.giveitup_be.mapper;

import org.mapstruct.Mapper;

import com.giveitup.giveitup_be.dto.request.PermissionRequest;
import com.giveitup.giveitup_be.dto.response.PermissionResponse;
import com.giveitup.giveitup_be.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
