package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.enums.PostStatus;
import com.giveitup.giveitup_be.mapper.PostMapper;
import com.giveitup.giveitup_be.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RecommendationService {

    RestTemplate restTemplate;
    PostRepository postRepository;
    PostMapper postMapper;
    UserService userService;
    PostService postService;
    // URL cho Gợi ý theo User (Trang chủ)
    String PYTHON_RCM_URL = "http://localhost:5000/getPostRecommender/";
    // [MỚI] URL cho Bài viết tương tự (Trang chi tiết)
    String PYTHON_SIMILAR_URL = "http://localhost:5000/getPostSimilar/";
    /**
     * Gợi ý bài viết cho User (Dựa trên lịch sử xem + User tương tự)
     * Dùng cho Trang Chủ
     */
    public List<PostResponse> getRecommendedPosts() {
        UserEntity user = userService.getMyInfoReturnEntity();

        try {
            log.info("--- Calling AI (Recommend) for User ID: {}", user.getId());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("userId", user.getId());
            requestBody.put("limit", 5);

            return fetchPostsFromPython(PYTHON_RCM_URL, requestBody, 5);

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Recommend: ", e);
            return postService.getTop5();
        }
    }

    /**
     * [MỚI] Tìm bài viết tương tự (Dựa trên nội dung bài viết hiện tại)
     * Dùng cho Trang Chi Tiết Bài Viết (Mục "Có thể bạn cũng thích")
     */
    public List<PostResponse> getSimilarPosts(Long currentPostId) {
        try {
            log.info("--- Calling AI (Similar) for Post ID: {}", currentPostId);
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("postId", currentPostId);
            return fetchPostsFromPython(PYTHON_SIMILAR_URL, requestBody, 3);
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI Similar: ", e);
            return postService.getTop3();
        }
    }

    // =========================================================================
    // HÀM CHUNG ĐỂ XỬ LÝ GỌI PYTHON VÀ MAP DỮ LIỆU (Tránh lặp code)
    // =========================================================================
    private List<PostResponse> fetchPostsFromPython(String url, Map<String, Object> requestBody, int count) {
        try {
            // 1. Gọi API Python
            List<?> rawIds = restTemplate.postForObject(url, requestBody, List.class);

            if (rawIds == null || rawIds.isEmpty()) {
                log.info("AI returned empty list. Fallback.");
                if(count == 3) {
                    return postService.getTop3();
                } else  {
                    return postService.getTop5();
                }
            }

            // 2. Ép kiểu an toàn từ Integer/Number sang Long
            List<Long> longIds = rawIds.stream()
                    .map(id -> ((Number) id).longValue())
                    .collect(Collectors.toList());

            // 3. Query DB
            List<PostEntity> posts = postRepository.findAllById(longIds);

            // 4. Map để lấy nhanh theo ID
            Map<Long, PostEntity> postMap = posts.stream()
                    .collect(Collectors.toMap(PostEntity::getId, p -> p));

            // 5. Sắp xếp lại theo đúng thứ tự Python trả về & Filter Active
            List<PostResponse> result = new ArrayList<>();
            for (Long id : longIds) {
                if (postMap.containsKey(id)) {
                    PostEntity post = postMap.get(id);
                    if (post.getStatus() != null && post.getStatus().equals(PostStatus.ACTIVE.getCode())) {
                        result.add(postMapper.toPostResponse(post));
                    }
                }
            }

            // Nếu sau khi lọc status mà danh sách rỗng -> Fallback
            if (result.isEmpty()) {
                return postService.getTop5();
            }

            return result;

        } catch (HttpClientErrorException.BadRequest e) {
            log.error("PYTHON API 400 Bad Request: {}", e.getResponseBodyAsString());
            throw e; // Ném ra để hàm cha bắt và gọi fallback
        }
    }
}