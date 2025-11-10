package com.giveitup.giveitup_be.mapper;


import com.giveitup.giveitup_be.dto.request.AuthorCreationRequest;
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
//    UserEntity toAuthor(AuthorCreationRequest request);
    @Mapping(source = "role.name", target = "role")
    UserResponse toUserResponse(UserEntity userEntity);

    @Mapping(target = "role", ignore = true)
    void updateUser(@MappingTarget UserEntity userEntity, UserUpdateRequest request);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "category", ignore = true )
    @Mapping(target = "organizationLogo", ignore = true)      // bỏ map MultipartFile
    @Mapping(target = "verificationFile", ignore = true)
    void updateAuthor(@MappingTarget UserEntity userEntity, AuthorCreationRequest request);
}
