package com.giveitup.giveitup_be.mapper;

import com.giveitup.giveitup_be.dto.response.CommentResponseForUser;
import com.giveitup.giveitup_be.entity.CommentEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PostMapper.class})
public interface CommentMapper {
        CommentResponseForUser toCommentResponseForUser(CommentEntity entity);
        List<CommentResponseForUser> toCommentResponseForUserList(List<CommentEntity> entities);

}
