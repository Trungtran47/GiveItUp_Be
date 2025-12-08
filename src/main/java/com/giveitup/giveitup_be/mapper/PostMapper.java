package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {PayoutMapper.class,RoleMapper.class})
public interface PostMapper {
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "video", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "payouts", ignore = true)
    PostEntity toPost(PostRequest request);
//    @Mapping(target = "payouts", ignore = true)
    @Mapping(target = "liked", ignore = true)
    PostResponse toPostResponse(PostEntity response);

    //    @Mapping(target = "role", ignore = true)
//    void updatePost(@MappingTarget PostEntity postEntity, PostRequest request);
}
