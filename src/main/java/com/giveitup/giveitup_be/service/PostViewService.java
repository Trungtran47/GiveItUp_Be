package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.PostViewEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.PostMapper;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.PostViewRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
public class PostViewService {
    PostViewRepository postViewRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    PostMapper postMapper;
    @Transactional // Quan trọng: Đảm bảo tính nhất quán dữ liệu khi update 2 bảng
    public void addView(Long userId, PostEntity post) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Long ownerId = post.getOrganization().getUser().getId();
        if (userId.equals(ownerId)) {
            log.info("User {} là chủ sở hữu bài viết {}, không tính lượt xem.", userId, post.getOrganization().getUser().getId());
            return;
        }
        try {
            PostViewEntity postView = postViewRepository.findFirstByPostIdAndUserId(post.getId(), userId)
                    .orElseGet(() -> {
                        PostViewEntity newView = new PostViewEntity();
                        newView.setPost(post);
                        newView.setUser(user);
                        newView.setViewCount(0L);
                        return postViewRepository.save(newView);
                    });

            postView.setViewCount(postView.getViewCount() + 1);
            postViewRepository.save(postView);
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent view update for user {} post {}", userId, post.getId());
        }
    }
    public Page<PostResponse> getPostsViewByUserId(Long userId, BasePagingRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, request.getPageSize());

        Page<Object[]> page = postRepository.findPostsAndPostViewsByUser(user, pageable);

        return page.map(obj -> {
            PostEntity post = (PostEntity) obj[0];
            LocalDateTime viewAt = (LocalDateTime) obj[1];
            PostResponse response = postMapper.toPostResponse(post);
            response.setViewAt(viewAt);
            return response;
        });
    }
}