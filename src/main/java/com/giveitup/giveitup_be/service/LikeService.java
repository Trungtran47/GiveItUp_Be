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
        return likeMapper.toLikeResponse(like);

    }

    public Page<PostResponse> getPostsByUserId(Long userId, BasePagingRequest request) {
        UserEntity user = userRepository.findById(String.valueOf(userId))
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
