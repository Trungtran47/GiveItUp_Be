package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.paging.PagingResponse;
import com.giveitup.giveitup_be.dto.request.ApiResponse;
import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.request.ReviewPostRequest;
import com.giveitup.giveitup_be.dto.request.SearchListPostRequest;
import com.giveitup.giveitup_be.dto.response.*;
import com.giveitup.giveitup_be.entity.LikeEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.PostViewEntity;
import com.giveitup.giveitup_be.entity.SearchHistoryEntity;
import com.giveitup.giveitup_be.enums.PostStatus;
import com.giveitup.giveitup_be.mapper.LikeMapper;
import com.giveitup.giveitup_be.repository.LikeRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.PostViewRepository;
import com.giveitup.giveitup_be.repository.SearchHistoryRepository;
import com.giveitup.giveitup_be.service.PostService;
import com.giveitup.giveitup_be.service.RecommendationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostController {
    PostService postService;
    LikeRepository  likeRepository;
    PostViewRepository postViewRepository;
    SearchHistoryRepository searchHistoryRepository;
    PostRepository postRepository;
    RecommendationService recommendationService;
    LikeMapper  likeMapper;
    @GetMapping("/recommend")
    public ApiResponse<List<PostResponse>> getRecommendations() {
        List<PostResponse> recommendedPosts = recommendationService.getRecommendedPosts();
        return ApiResponse.<List<PostResponse>>builder()
                .result(recommendedPosts)
                .build();
    }
    @GetMapping("/{id}/related")
    public ApiResponse<List<PostResponse>> getRelatedPosts(@PathVariable Long id) {
        List<PostResponse> recommendedPosts = recommendationService.getSimilarPosts(id);
        return ApiResponse.<List<PostResponse>>builder()
                .result(recommendedPosts)
                .build();
    }
    @GetMapping("/getRcmPostDB")
    public ResponseEntity<?> getRcmPostDB() {
        Map<String, Object> data = new HashMap<>();

        // 1. Lấy Post và Map sang DTO
        // Lưu ý: postService.getAllPostsForAI() nên trả về List<PostEntity>
        List<PostEntity> posts = postRepository.findAllByStatus(PostStatus.ACTIVE.getCode()); // Hoặc dùng hàm service cũ của bạn
        List<PostAIResponse> postDTOs = posts.stream()
                .map(p -> PostAIResponse.builder()
                        .postId(p.getId())
                        .title(p.getTitle())
                        .content(p.getDescription()) // Cẩn thận nếu content quá dài, có thể cắt bớt
                        .createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        data.put("postDb", postDTOs);

        // 2. Lấy Like và Map sang DTO (Cắt đứt quan hệ User-Like-User)
        List<LikeEntity> likes = likeRepository.findAll();
        List<InteractionAIResponse> likeDTOs = likes.stream()
                .map(l -> InteractionAIResponse.builder()
                        .userId(l.getUser().getId())  // Lấy ID trực tiếp
                        .postId(l.getPost().getId())  // Lấy ID trực tiếp
                        .createdAt(l.getCreatedAt())
                        .viewCount(null)              // Like không có view count
                        .build())
                .collect(Collectors.toList());
        data.put("postLikeDb", likeDTOs);

        // 3. Lấy View và Map sang DTO (Khắc phục lỗi Recursion tại đây)
        List<PostViewEntity> views = postViewRepository.findAll();
        List<InteractionAIResponse> viewDTOs = views.stream()
                .map(v -> InteractionAIResponse.builder()
                        .userId(v.getUser().getId())
                        .postId(v.getPost().getId())
                        .createdAt(v.getCreatedAt())
                        .viewCount(v.getViewCount())
                        .build())
                .collect(Collectors.toList());
        data.put("postViewDb", viewDTOs);

        // 4. Lấy Search History và Map sang DTO
//        List<SearchHistoryEntity> searches = searchHistoryRepository.findAll();
//        List<SearchAIResponse> searchDTOs = searches.stream()
//                .map(s -> SearchAIResponse.builder()
//                        .userId(s.getUser().getId())
//                        .keyword(s.getKeyword())
//                        .createdAt(s.getCreatedAt())
//                        .build())
//                .collect(Collectors.toList());
//        data.put("searchHistoryDb", searchDTOs);

        return ResponseEntity.ok(data);
    }
    @GetMapping("/top5")
    public ApiResponse<List<PostResponse>> getTop5Post() {
        List<PostResponse> result = postService.getTop5();
        return ApiResponse.<List<PostResponse>>builder()
                .result(result)
                .build();
    }
    @GetMapping("/map")
    public ApiResponse<List<PostMapResponse>> getPostsOnMap(@RequestParam String city) {
        List<PostMapResponse> result = postService.getPostsByCity(city);
        return ApiResponse.<List<PostMapResponse>>builder()
                .result(result)
                .build();
    }
    @PostMapping(path = "/create", consumes = {"multipart/form-data"})
    public ApiResponse<PostResponse> createPost(@ModelAttribute PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request))
                .build();
    }

    @PutMapping(path ="/update/{postId}", consumes = {"multipart/form-data"})
    public ApiResponse<PostResponse> updatePost(@PathVariable Long postId, @ModelAttribute PostRequest request) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.updatePost(postId,request))
                .build();
    }
    @DeleteMapping("/delete/{postId}")
    ApiResponse<String> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.<String>builder()
                .result("Post has been deleted")
                .build();
    }
    @GetMapping("/organization/{organizationId}")
    ApiResponse<PagingResponse<PostResponse>> getPostByOrganizationId(@PathVariable Long organizationId, SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPostByOrganizationId(organizationId,request);
        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<PostResponse> pagingResponse = PagingResponse.<PostResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<PostResponse>>builder()
                .result(pagingResponse)
                .build();
    }
    @GetMapping
    ApiResponse<PagingResponse<PostResponse>> getPosts(@ModelAttribute SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPosts(request);

        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<PostResponse> pagingResponse = PagingResponse.<PostResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<PostResponse>>builder()
                .result(pagingResponse)
                .build();
    }
    @GetMapping("/admin")
    ApiResponse<PagingResponse<PostResponse>> getPostsAdmin(@ModelAttribute SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPostsAdmin(request);

        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<PostResponse> pagingResponse = PagingResponse.<PostResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<PostResponse>>builder()
                .result(pagingResponse)
                .build();
    }
    @GetMapping("/categoryId/{categoryId}")
    ApiResponse<PagingResponse<PostResponse>> getPostsByCategoryId(@PathVariable Long categoryId,@ModelAttribute SearchListPostRequest request) {
        Page<PostResponse> page = postService.getPostsByCategory(categoryId, request);

        PagingResponse.PagingInfo paging = PagingResponse.PagingInfo.builder()
                .CurrentPage(request.getCurrentPage())
                .NumberOfRecord(request.getPageSize())
                .TotalRecord(page.getTotalElements())
                .TotalPages(page.getTotalPages())
                .build();

        PagingResponse<PostResponse> pagingResponse = PagingResponse.<PostResponse>builder()
                .Paging(paging)
                .Data(page.getContent())
                .build();
        return ApiResponse.<PagingResponse<PostResponse>>builder()
                .result(pagingResponse)
                .build();
    }
    @GetMapping("/{postId}")
    ApiResponse<PostResponse> getPostById(@PathVariable Long postId) {
        return ApiResponse.<PostResponse>builder()
                .result(postService.getPostById(postId))
                .build();
    }
    @PutMapping("/update-status/{postId}")
    public ApiResponse<String> reviewPost(@PathVariable Long postId,
                                          @RequestBody ReviewPostRequest request) {
        postService.reviewPost(postId, request);
        return ApiResponse.<String>builder()
                .result("Trạng thái đã được cập nhật")
                .build();
    }
//    @GetMapping("/{id}/related")
//    public ApiResponse<List<PostResponse>> getRelatedPosts(@PathVariable Long id) {
//        // 1. Lấy thông tin bài viết hiện tại để biết categoryId
//        PostResponse currentPost = postService.getPostById(id);
//
//        // 2. Gọi hàm recommend
//        List<PostResponse> related = postService.getRelatedPosts(currentPost.getCategory().getId(), id);
//
//        return ApiResponse.<List<PostResponse>>builder()
//                .result(related)
//                .build();
//    }
}
