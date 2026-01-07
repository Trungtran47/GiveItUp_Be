package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.dto.response.SearchResponse;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.SearchHistoryEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.mapper.PostMapper;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.SearchHistoryRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import com.giveitup.giveitup_be.specification.PostSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SearchService {

      PostRepository postRepository;
      UserRepository userRepository;
      SearchHistoryRepository searchHistoryRepository;
      UserService userService; // Lấy user đang login
      PostMapper postMapper;
      RedisService redisService;

    public Page<PostResponse> search(String keyword, BasePagingRequest request) {
        // Nếu không có keyword → trả về trang rỗng
        if (keyword == null || keyword.trim().isEmpty()) {
            return Page.empty();
        }
        // 1. Lưu lịch sử
        UserEntity currentUser = userService.getMyInfoReturnEntity();
        searchHistoryRepository.save(
                SearchHistoryEntity.builder()
                        .keyword(keyword)
                        .user(currentUser)
                        .type("mix")
                        .build()
        );
        // 2. Tạo Pageable
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, request.getPageSize());
        // 3. Tạo Specification không deprecated
        Specification<PostEntity> spec = Specification.allOf(
                PostSpecification.containsKeyword(keyword)
        );
        // 4. Query phân trang
        Page<PostEntity> posts = postRepository.findAll(spec, pageable);
        // =================================================================
        // [START] REDIS LOGIC: LƯU KẾT QUẢ TÌM KIẾM CHO AI
        // =================================================================
        if (currentUser != null && posts.hasContent()) {
            // Lấy danh sách ID của các bài viết tìm được ở trang hiện tại
            List<Long> foundPostIds = posts.getContent().stream()
                    .map(PostEntity::getId)
                    .collect(Collectors.toList());

            CompletableFuture.runAsync(() -> {
                try {
                    // Gọi hàm mới trong RedisService
                    redisService.saveSearchHistory(currentUser.getId(), foundPostIds);
                } catch (Exception e) {
                    log.error("Error saving search history to Redis", e);
                }
            });
        }
        // =================================================================
        // [END] REDIS LOGIC
        // =================================================================
        // 5. Map sang Page<PostResponse>
        return posts.map(postMapper::toPostResponse);
    }



    public List<SearchResponse.HistoryResponse> getSearchHistory() {
        UserEntity currentUser = userService.getMyInfoReturnEntity();
        return searchHistoryRepository
                .findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(h -> {
                    SearchResponse.HistoryResponse dto = new SearchResponse.HistoryResponse();
                    dto.setId(h.getId());
                    dto.setKeyword(h.getKeyword());
                    dto.setCreatedAt(h.getCreatedAt());
                    return dto;
                })
                .toList();
    }
    public void deleteSearchHistory(Long historyId) {
        UserEntity currentUser = userService.getMyInfoReturnEntity();
        SearchHistoryEntity history = searchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new RuntimeException("History không tồn tại"));
        if (!history.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xoá lịch sử này");
        }
        searchHistoryRepository.delete(history);
    }
    @Transactional
    public void deleteAllSearchHistory() {
        UserEntity currentUser = userService.getMyInfoReturnEntity();
        searchHistoryRepository.deleteAllByUserId(currentUser.getId());
    }


    private SearchResponse.PostSearchResponse toPostDTO(PostEntity p) {
        SearchResponse.PostSearchResponse dto = new SearchResponse.PostSearchResponse();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setTargetAmount(p.getTargetAmount());
        dto.setDonatedAmount(p.getDonatedAmount());

        dto.setImage(
                p.getImages() != null && !p.getImages().isEmpty()
                        ? p.getImages().get(0).getImageUrl()
                        : null
        );

        return dto;
    }

    private SearchResponse.UserSearchResponse toUserDTO(UserEntity u) {
        SearchResponse.UserSearchResponse dto = new SearchResponse.UserSearchResponse();
        dto.setId(u.getId());
        dto.setFullName(u.getFirstName() + " " + u.getLastName());
        dto.setImageUser(u.getImageUser());
        dto.setOrganizationName(u.getOrganization().getOrganizationName());
        return dto;
    }


}
