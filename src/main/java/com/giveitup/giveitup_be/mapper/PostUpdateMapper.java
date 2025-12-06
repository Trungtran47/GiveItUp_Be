package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.response.PayoutResponse;
import com.giveitup.giveitup_be.dto.response.PostUpdateResponse;
import com.giveitup.giveitup_be.entity.PayoutEntity;
import com.giveitup.giveitup_be.entity.PostUpdateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostUpdateMapper {

    @Mapping(source = "post.id",   target = "postId")
    @Mapping(source = "payout.id", target = "payoutId")
    PostUpdateResponse toPostUpdateResponse(PostUpdateEntity postUpdate);
}
