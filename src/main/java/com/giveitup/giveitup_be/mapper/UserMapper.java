package com.giveitup.giveitup_be.mapper;


import com.giveitup.giveitup_be.dto.request.SearchListUserRequest;
import com.giveitup.giveitup_be.dto.request.UserCreationRequest;
import com.giveitup.giveitup_be.dto.request.UserUpdateRequest;
import com.giveitup.giveitup_be.dto.response.UserResponse;
import com.giveitup.giveitup_be.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(source = "role", target = "role.name")
    UserEntity toUser(UserCreationRequest request);

    UserResponse toUserResponse(UserEntity userEntity);

    @Mapping(target = "role", ignore = true)
    void updateUser(@MappingTarget UserEntity userEntity, UserUpdateRequest request);
}
