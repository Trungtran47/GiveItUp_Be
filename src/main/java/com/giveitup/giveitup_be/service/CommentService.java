package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.CommentRequest;
import com.giveitup.giveitup_be.dto.request.CommentResponse;
import com.giveitup.giveitup_be.dto.request.SearchListDonateRequest;
import com.giveitup.giveitup_be.dto.response.CommentResponseForUser;
import com.giveitup.giveitup_be.dto.response.DonateResponse;
import com.giveitup.giveitup_be.entity.*;
import com.giveitup.giveitup_be.enums.ReactionType;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.CommentMapper;
import com.giveitup.giveitup_be.mapper.UserMapper;
import com.giveitup.giveitup_be.repository.*;
import com.giveitup.giveitup_be.specification.DonateSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final CommentMapper commentMapper;
    private final CommentReactionRepository reactionRepository;

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

//    public List<CommentResponse> getCommentsByPost(Long postId) {
//        return commentRepository.findByPostIdAndParentCommentIsNull(postId)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }

public List<CommentResponse> getCommentsByPost(Long postId) {
    List<CommentEntity> rootComments = commentRepository.findByPostIdAndParentCommentIsNull(postId);
    Long currentUserId = null;
    try {
        UserEntity currentUser = userService.getMyInfoReturnEntity();
        if (currentUser != null) currentUserId = currentUser.getId();
    } catch (Exception e) {
    }
    Map<Long, String> userReactionMap = new HashMap<>();
    if (currentUserId != null) {
        List<CommentReactionEntity> reactions = reactionRepository.findAllByUserIdAndPostId(currentUserId, postId);
        for (CommentReactionEntity reaction : reactions) {
            userReactionMap.put(reaction.getComment().getId(), reaction.getType().name());
        }
    }
    Map<Long, String> finalReactionMap = userReactionMap;
    return rootComments.stream()
            .map(comment -> mapToResponse(comment, finalReactionMap))
            .collect(Collectors.toList());
}
    private CommentResponse mapToResponse(CommentEntity entity, Map<Long, String> reactionMap) {
        return CommentResponse.builder()
                .id(entity.getId())
                .content(entity.getContent())
                .userId(entity.getUser().getId())
                .userName(entity.getUser().getLastName() + " " + entity.getUser().getFirstName()) // Ví dụ
                .avatar(entity.getUser().getImageUser())
                .postId(entity.getPost().getId())
                .createdAt(entity.getCreatedAt())
                // Set Like/Dislike count từ Entity
                .likeCount(entity.getLikeCount())
                .dislikeCount(entity.getDislikeCount())
                // Set myReaction: Lấy từ Map truyền vào (O(1) lookup)
                .myReaction(reactionMap.get(entity.getId()))
                // Đệ quy cho replies (cũng cần truyền reactionMap xuống con)
                .replies(entity.getReplies() == null ? new ArrayList<>() :
                        entity.getReplies().stream()
                                .map(reply -> mapToResponse(reply, reactionMap)) // Truyền tiếp map xuống
                                .collect(Collectors.toList()))
                .build();
    }
   public List<CommentResponseForUser>  getCommentsByUserId() {
        List<CommentEntity> entities = commentRepository.findByUserIdOrderByCreatedAtDesc(userService.getMyInfoReturnEntity().getId());
        return commentMapper.toCommentResponseForUserList(entities);
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
            response.setAvatar(comment.getUser().getOrganization().getOrganizationLogo());
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
    @Transactional
    public void reactToComment(Long commentId, ReactionType newType) {
        UserEntity user = userService.getMyInfoReturnEntity();
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // 1. Kiểm tra xem user đã tương tác với comment này chưa
        Optional<CommentReactionEntity> existingReactionOpt =
                reactionRepository.findByUserIdAndCommentId(user.getId(), commentId);

        if (existingReactionOpt.isPresent()) {
            CommentReactionEntity existingReaction = existingReactionOpt.get();

            if (existingReaction.getType() == newType) {
                // TRƯỜNG HỢP 1: Bấm lại nút đã chọn -> Gỡ bỏ (Toggle OFF)
                reactionRepository.delete(existingReaction);

                if (newType == ReactionType.LIKE) comment.decrementLike();
                else comment.decrementDislike();

            } else {
                // TRƯỜNG HỢP 2: Đổi từ Like sang Dislike (hoặc ngược lại)
                existingReaction.setType(newType);
                reactionRepository.save(existingReaction);

                if (newType == ReactionType.LIKE) {
                    comment.incrementLike();
                    comment.decrementDislike();
                } else {
                    comment.incrementDislike();
                    comment.decrementLike();
                }
            }
        } else {
            // TRƯỜNG HỢP 3: Chưa tương tác bao giờ -> Tạo mới
            CommentReactionEntity newReaction = CommentReactionEntity.builder()
                    .user(user)
                    .comment(comment)
                    .type(newType)
                    .build();
            reactionRepository.save(newReaction);

            if (newType == ReactionType.LIKE) comment.incrementLike();
            else comment.incrementDislike();
        }
        commentRepository.save(comment);
    }
}
