package com.giveitup.giveitup_be.mapper;


import com.giveitup.giveitup_be.dto.request.RoleRequest;
import com.giveitup.giveitup_be.dto.response.RoleResponse;
import com.giveitup.giveitup_be.entity.RoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    RoleEntity toRole(RoleRequest request);

    RoleResponse toRoleResponse(RoleEntity roleEntity);
}
