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
    private final NotificationService notificationService;
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
        // 3. GỬI THÔNG BÁO (Trong khối try-catch để không ảnh hưởng luồng chính)
        try {
            UserEntity sender = user;
            String senderName = getUserDisplayName(sender);
            String link = "/project/" + post.getId();

            // Logic xác định người nhận
            if (request.getParentCommentId() != null) {
                // A. Trường hợp Reply: Gửi cho người mình đang trả lời
                // Lưu ý: Lấy parent từ biến comment vừa set ở trên
                CommentEntity parent = comment.getParentComment();
                UserEntity parentOwner = parent.getUser();

                // Chỉ gửi nếu người trả lời KHÁC người tạo comment gốc (tránh tự kỷ)
                if (!parentOwner.getId().equals(sender.getId())) {
                    notificationService.sendNotification(
                            parentOwner,
                            sender,
                            senderName + " đã trả lời bình luận của bạn.",
                            "REPLY_COMMENT",
                            link
                    );
                }
            } else {
                // B. Trường hợp Comment bài viết: Gửi cho chủ bài viết
                UserEntity postOwner = post.getOrganization().getUser();

                // Chỉ gửi nếu người comment KHÁC chủ bài viết
                if (!postOwner.getId().equals(sender.getId())) {
                    notificationService.sendNotification(
                            postOwner,
                            sender,
                            senderName + " đã bình luận về bài viết của bạn.",
                            "COMMENT_POST",
                            link
                    );
                }
            }
        } catch (Exception e) {
            // Log lỗi notify nhưng vẫn return success cho comment
            System.err.println("⚠️ Lỗi gửi thông báo comment: " + e.getMessage());
            // e.printStackTrace(); // Bỏ comment nếu muốn xem full stack trace
        }
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
                .userName(getUserDisplayName(entity.getUser())) //  Dùng hàm helper
                .avatar(getUserAvatar(entity.getUser())) // Truyền biến đã xử lý ở trên
                .postId(entity.getPost().getId())
                .createdAt(entity.getCreatedAt())
                .likeCount(entity.getLikeCount())
                .dislikeCount(entity.getDislikeCount())
                .myReaction(reactionMap.get(entity.getId()))
                .replies(entity.getReplies() == null ? new ArrayList<>() :
                        entity.getReplies().stream()
                                .map(reply -> mapToResponse(reply, reactionMap))
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
        //  Dùng hàm helper cho gọn
        response.setUserName(getUserDisplayName(comment.getUser()));
        response.setAvatar(getUserAvatar(comment.getUser()));

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
        boolean isNewLike = false; // Cờ đánh dấu để gửi thông báo
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
                    isNewLike = true; // Chuyển sang Like cũng cần báo
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

            if (newType == ReactionType.LIKE) {
                comment.incrementLike();
                isNewLike = true;
            } else {
                comment.incrementDislike();
            }
        }
        commentRepository.save(comment);
        // 3. GỬI THÔNG BÁO KHI CÓ LIKE (Chỉ gửi Like, Dislike thường không gửi để tránh toxic)
        if (isNewLike) {
            try {
                UserEntity recipient = comment.getUser();
                if (!recipient.getId().equals(user.getId())) {
                    String senderName = getUserDisplayName(user);
                    String contentSnippet = truncateContent(comment.getContent());
                    String message = senderName + " đã thích bình luận của bạn: \"" + contentSnippet + "\"";
                    String link = "/project/" + comment.getPost().getId();
                    notificationService.sendNotification(recipient, user, message, "LIKE_COMMENT", link);
                }
            } catch (Exception e) {
                System.err.println("⚠️ Lỗi gửi thông báo Like: " + e.getMessage());
            }
        }
    }
    // Hàm phụ trợ: Cắt ngắn nội dung comment để hiện trong thông báo
    private String truncateContent(String content) {
        if (content == null) return "";
        return content.length() > 20 ? content.substring(0, 20) + "..." : content;
    }
    // --- HÀM HELPER: LẤY TÊN HIỂN THỊ (USER hoặc ORGANIZATION) ---
    private String getUserDisplayName(UserEntity user) {
        // Giả sử ID role AUTHOR là "AUTHOR" như trong code cũ của bạn
        if (user.getRole() != null && "AUTHOR".equals(user.getRole().getName())) {
            if (user.getOrganization() != null) {
                return user.getOrganization().getOrganizationName();
            }
        }
        return user.getLastName() + " " + user.getFirstName();
    }

    // --- HÀM HELPER: LẤY AVATAR (USER hoặc ORGANIZATION) ---
    private String getUserAvatar(UserEntity user) {
        if (user.getRole() != null && "AUTHOR".equals(user.getRole().getName())) {
            if (user.getOrganization() != null) {
                return user.getOrganization().getOrganizationLogo();
            }
        }
        return user.getImageUser();
    }
}
