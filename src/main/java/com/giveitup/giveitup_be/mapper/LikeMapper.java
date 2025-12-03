package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.response.LikeResponse;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.LikeEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LikeMapper {

    @Mapping(source = "user.id",       target = "userId")
    @Mapping(source = "user.username", target = "userName")
    @Mapping(source = "post.id",       target = "postId")
    @Mapping(source = "post.likeCount", target = "likeCount")
    @Mapping(target = "liked", constant = "true") // Vì LikeEntity tồn tại nghĩa là đã like
    LikeResponse toLikeResponse(LikeEntity entity);
}

