package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import com.giveitup.giveitup_be.dto.response.LikeResponse;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.LikeEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.LikeMapper;
import com.giveitup.giveitup_be.mapper.PostMapper;
import com.giveitup.giveitup_be.repository.LikeRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class LikeService {
    LikeRepository likeRepository;
    LikeMapper likeMapper;
    PostRepository postRepository;
    UserRepository userRepository;
    PostMapper postMapper;
    UserService userService;
    NotificationService notificationService;
    RedisService redisService;
    @Transactional
    public LikeResponse toggleLike(Long postId) {
        UserEntity user = userService.getMyInfoReturnEntity();
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        // Kiểm tra đã like chưa
        LikeEntity existingLike =
                likeRepository.findLikeEntitiesByPostIdAndUserId(post.getId(), user.getId());
        // ----- CASE 1: User ĐÃ LIKE -> UNLIKE -----
        if (existingLike != null) {
            likeRepository.delete(existingLike);
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            // [REDIS FIX]: XÓA KHỎI REDIS KHI UNLIKE
            CompletableFuture.runAsync(() -> {
                redisService.removeLikeHistory(user.getId(), post.getId());
            });
            return LikeResponse.builder()
                    .postId(post.getId())
                    .userId(user.getId())
                    .userName(user.getUsername())
                    .likeCount(post.getLikeCount())
                    .liked(false)
                    .build();

        }
        // ----- CASE 2: User CHƯA LIKE -> LIKE -----
        LikeEntity like = LikeEntity.builder()
                .user(user)
                .post(post)
                .build();
        likeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);

        // [REDIS FIX]: LƯU VÀO LIST "LIKES" RIÊNG
        CompletableFuture.runAsync(() -> {
            redisService.saveLikeHistory(user.getId(), post.getId());
        });
        // =================================================================
        // [END] REDIS LOGIC
        // =================================================================
        // --- [START] GỬI THÔNG BÁO ---
        // Chỉ gửi thông báo nếu người like KHÔNG PHẢI là chủ bài viết
        if (!user.getId().equals(post.getOrganization().getUser().getId())) {

            // 1. Xử lý tên hiển thị (Logic Organization hoặc User thường)
            String displayName;
            if (user.getOrganization() != null) {
                // Thay .getOrgName() bằng getter thực tế trong OrganizationEntity của bạn
                displayName = user.getOrganization().getOrganizationName();
            } else {
                String firstName = user.getFirstName() == null ? "" : user.getFirstName();
                String lastName = user.getLastName() == null ? "" : user.getLastName();
                displayName = (firstName + " " + lastName).trim();
                if (displayName.isEmpty()) displayName = user.getUsername();
            }

            // 2. Gửi thông báo
            notificationService.sendNotification(
                    post.getOrganization().getUser(),                     // Người nhận: Chủ bài viết
                    user,                               // Người gửi: Người vừa like
                    displayName + " đã thích bài viết của bạn.", // Nội dung
                    "LIKE",                             // Loại thông báo
                    "/project/" + post.getId()          // Link tới bài viết (VD: /project/123 hoặc /post/123)
            );
        }
        // --- [END] GỬI THÔNG BÁO ---
        return likeMapper.toLikeResponse(like);

    }

    public Page<PostResponse> getPostsByUserId(Long userId, BasePagingRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, request.getPageSize());

        Page<Object[]> page = postRepository.findPostsAndLikeTimeByUser(user, pageable);

        return page.map(obj -> {
            PostEntity post = (PostEntity) obj[0];
            LocalDateTime likedAt = (LocalDateTime) obj[1];
            PostResponse response = postMapper.toPostResponse(post);
            response.setLikedAt(likedAt);
            response.setLiked(true); // chắc chắn là user đã like
            return response;
        });
    }

}
