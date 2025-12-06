package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.CommentRequest;
import com.giveitup.giveitup_be.dto.request.CommentResponse;
import com.giveitup.giveitup_be.entity.CommentEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.RoleEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.UserMapper;
import com.giveitup.giveitup_be.repository.CommentRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.RoleRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Transactional
    public CommentResponse createComment(Long userId, CommentRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PostEntity post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        CommentEntity comment = new CommentEntity();
        comment.setContent(request.getContent());
        comment.setUser(user);
        comment.setPost(post);

        if (request.getParentCommentId() != null) {
            CommentEntity parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new AppException(ErrorCode.PARENT_COMMENT_NOT_FOUND));
            comment.setParentComment(parent);
        }

        CommentEntity saved = commentRepository.save(comment);
        return mapToResponse(saved);
    }

    public List<CommentResponse> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdAndParentCommentIsNull(postId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.PARENT_COMMENT_NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Cannot delete comment of another user");
        }
        commentRepository.delete(comment);
    }

    private CommentResponse mapToResponse(CommentEntity comment) {
        RoleEntity roleAuthor =  roleRepository.findById("AUTHOR").orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_EXISTED));
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setContent(comment.getContent());
        response.setUserId(comment.getUser().getId());
        if (comment.getUser().getRole() != null &&
                comment.getUser().getRole().getName().equals(roleAuthor.getName())) {
            response.setAvatar(comment.getUser().getOrganizationLogo());
        } else {
            response.setAvatar(comment.getUser().getImageUser());
        }
        response.setUserName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        response.setPostId(comment.getPost().getId());
        response.setCreatedAt(comment.getCreatedAt());
        if (comment.getReplies() != null) {
            response.setReplies(comment.getReplies().stream().map(this::mapToResponse).collect(Collectors.toList()));
        }
        return response;
    }
}
