package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.response.FollowResponse;
import com.giveitup.giveitup_be.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FollowMapper {

    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    FollowResponse toFollowResponse(UserEntity user);
}
