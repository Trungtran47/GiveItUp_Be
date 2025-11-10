package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostEntity toPost(PostRequest request);
    @Mapping(target = "user", ignore = true)
    PostResponse toPostResponse(PostEntity response);

    //    @Mapping(target = "role", ignore = true)
    void updatePost(@MappingTarget PostEntity postEntity, PostRequest request);
}
