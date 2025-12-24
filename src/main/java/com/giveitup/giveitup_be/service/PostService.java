package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.request.ReviewPostRequest;
import com.giveitup.giveitup_be.dto.request.SearchListPostRequest;
import com.giveitup.giveitup_be.dto.response.PayoutResponse;
import com.giveitup.giveitup_be.dto.response.PostMapResponse;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.entity.*;
import com.giveitup.giveitup_be.enums.PayoutStatus;
import com.giveitup.giveitup_be.enums.PostStatus;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.PostMapper;
import com.giveitup.giveitup_be.mapper.PostUpdateMapper;
import com.giveitup.giveitup_be.repository.*;
import com.giveitup.giveitup_be.specification.PostSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostService {
    PostRepository postRepository;
    PostMapper postMapper;
    CloudinaryService cloudinaryService;
    CategoryRepository categoryRepository;
    BankAccountRepository bankAccountRepository;
    UserRepository userRepository;
    PostViewService postViewService;
    LikeRepository likeRepository;
    PostUpdateMapper postUpdateMapper;
    DonateRepository donateRepository;
    ImageRepository imageRepository;
    NotificationService notificationService;
    OrganizationRepository organizationRepository;
    private final UserService userService;
    public List<PostResponse> getTop5( ){
        List<PostEntity> post = postRepository.findTop5ByStatusOrderByDonatedAmountDesc(PostStatus.ACTIVE.getCode());
        return postMapper.toPostResponseList(post);
    }
    public List<PostMapResponse> getPostsByCity(String city) {
        Long activeStatus = 20L;
        List<PostEntity> posts = postRepository.searchByAddress(city, activeStatus);
        return posts.stream().map(post -> {
            String thumbnail = null;
            if (post.getImages() != null && !post.getImages().isEmpty()) {
                thumbnail = post.getImages().get(0).getImageUrl();
            }
            return PostMapResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .address(post.getAddress())
                    .image(thumbnail)
                    .categoryName(post.getCategory().getCategoryName())
                    .latitude(null)
                    .longitude(null)
                    .build();
        }).collect(Collectors.toList());
    }
    @PreAuthorize("hasRole('AUTHOR')")
    public PostResponse createPost(PostRequest request) {
        // B1. Map request sang entity
        PostEntity postEntity = postMapper.toPost(request);
        try {
            // B2. Lưu post trước (để có ID)
            OrganizationEntity organizationEntity = organizationRepository.findById(request.getOrganization()).orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_NOT_EXISTED));

            CategoryEntity categoryEntity = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED));
            BankAccountEntity bankAccountEntity = bankAccountRepository.findById(request.getBankAccount()).
                    orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_EXISTED));

            postEntity.setOrganization(organizationEntity);
            postEntity.setCategory(categoryEntity);
            postEntity.setBankAccount(bankAccountEntity);
            postEntity.setDonatedAmount(0.0);
            postEntity.setLikeCount(0L);    // bắt buộc
            postEntity.setViewCount(0L);
            postEntity.setStatus(PostStatus.PENDING.getCode());
            postEntity.setEndDate(request.getEndDate());
            postEntity.setStatusName(PostStatus.PENDING.getLabel());
            postEntity = postRepository.save(postEntity);
            // B3. Nếu có file ảnh thì upload
            if (request.getVideo() != null && !request.getVideo().isEmpty()) {
                Map<String, String> uploadResult = cloudinaryService.uploadVideo(
                        request.getVideo(), "GiveItUp/posts/video");
                postEntity.setVideo(uploadResult.get("url"));
                postEntity.setPublicVideoId(uploadResult.get("public_id"));
            }
//            if (request.getVideoUrl() != null && !request.getVideoUrl().isEmpty()) {
//                postEntity.setVideo(request.getVideoUrl());
//            }

            List<PostRequest.ImageRequest> images = request.getImages();
            if (images != null && !images.isEmpty()) {
                List<ImageEntity> imageEntities = new ArrayList<>();
                for (int i = 0; i < images.size(); i++) {
                    PostRequest.ImageRequest imageRequest = images.get(i);
                    MultipartFile file = imageRequest.getFile();
                    // Nếu có file mới thì upload
                    if (file != null && !file.isEmpty()) {
                        Map<String, String> uploadResult = cloudinaryService.uploadImage(file, "GiveItUp/posts/image");
                        ImageEntity image = ImageEntity.builder()
                                .imageUrl(uploadResult.get("url"))
                                .publicId(uploadResult.get("public_id"))
                                .isThumbnail(imageRequest.isThumbnail() || i == 0) // ảnh được chọn FE hoặc ảnh đầu tiên
                                .post(postEntity)
                                .build();
                        imageEntities.add(image);
                    }
                }
                postEntity.getImages().addAll(imageEntities);
                postRepository.save(postEntity);
            }

        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.POST_EXISTED);
        }

        return postMapper.toPostResponse(postEntity);
    }

    @PreAuthorize("hasRole('AUTHOR')")
    @Transactional
    public PostResponse updatePost(Long postId, PostRequest request) {
        // Lấy post từ DB, bao gồm ảnh
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        // ==== B1. CẬP NHẬT CÁC FIELD CƠ BẢN ====
        postEntity.setTitle(request.getTitle());
        postEntity.setDescription(request.getDescription());
        postEntity.setTargetAmount(request.getTargetAmount());
        postEntity.setAddress(request.getAddress());
        postEntity.setEndDate(request.getEndDate());
        postEntity.setCategory(categoryRepository.findById(request.getCategory())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)));
        postEntity.setBankAccount(bankAccountRepository.findById(request.getBankAccount())
                .orElseThrow(() -> new AppException(ErrorCode.BANK_ACCOUNT_NOT_EXISTED)));
//        if (request.getEndDate().isAfter(LocalDateTime.now())) {
//            postEntity.setStatus(PostStatus.ACTIVE.getCode());
//            postEntity.setStatusName(PostStatus.ACTIVE.getLabel());
//        }

        // ==== B2. XỬ LÝ VIDEO ====
        MultipartFile newVideoFile = request.getVideo();
        boolean sentVideoId = request.getPublicVideoId() != null && !request.getPublicVideoId().isEmpty();

        if (newVideoFile != null && !newVideoFile.isEmpty()) {
            if (postEntity.getPublicVideoId() != null) {
                cloudinaryService.deleteFile(postEntity.getPublicVideoId(), false);
            }
            Map<String, String> uploadVideo = cloudinaryService.uploadVideo(newVideoFile, "GiveItUp/posts/video");
            postEntity.setVideo(uploadVideo.get("url"));
            postEntity.setPublicVideoId(uploadVideo.get("public_id"));
        } else if (!sentVideoId) {
            if (postEntity.getPublicVideoId() != null) {
                cloudinaryService.deleteVideo(postEntity.getPublicVideoId());
            }
            postEntity.setVideo(null);
            postEntity.setPublicVideoId(null);
        }
        // Nếu FE gửi publicVideoId → giữ nguyên video

        // ==== B3. XỬ LÝ IMAGE ====
        List<PostRequest.ImageRequest> reqImages = request.getImages();
        List<ImageEntity> oldImages = new ArrayList<>(postEntity.getImages());

        if (reqImages == null || reqImages.isEmpty()) {
            // Xóa tất cả ảnh cũ
            for (ImageEntity img : oldImages) {
                if (img.getPublicId() != null) {
                    cloudinaryService.deleteImage(img.getPublicId());
                }
            }
            postEntity.getImages().clear(); // Hibernate sẽ tự delete nhờ orphanRemoval
        } else {
            // 1. Xóa ảnh cũ không còn trong request
            Iterator<ImageEntity> iterator = postEntity.getImages().iterator();
            while (iterator.hasNext()) {
                ImageEntity oldImg = iterator.next();
                boolean existsInRequest = reqImages.stream()
                        .anyMatch(img -> img.getPublicId() != null && img.getPublicId().equals(oldImg.getPublicId()));
                if (!existsInRequest) {
                    if (oldImg.getPublicId() != null) {
                        cloudinaryService.deleteImage(oldImg.getPublicId());
                    }
                    iterator.remove(); // orphanRemoval sẽ xoá DB
                }
            }

            // 2. Thêm ảnh mới / cập nhật thumbnail
            for (PostRequest.ImageRequest img : reqImages) {
                if (img.getPublicId() != null && !img.getPublicId().isEmpty()) {
                    // Ảnh cũ, cập nhật thumbnail
                    oldImages.stream()
                            .filter(o -> o.getPublicId().equals(img.getPublicId()))
                            .findFirst()
                            .ifPresent(o -> o.setIsThumbnail(img.isThumbnail()));
                } else if (img.getFile() != null && !img.getFile().isEmpty()) {
                    // Ảnh mới
                    Map<String, String> upload = cloudinaryService.uploadImage(img.getFile(), "GiveItUp/posts/image");
                    ImageEntity uploaded = ImageEntity.builder()
                            .imageUrl(upload.get("url"))
                            .publicId(upload.get("public_id"))
                            .isThumbnail(img.isThumbnail())
                            .post(postEntity)
                            .build();
                    postEntity.getImages().add(uploaded);
                }
            }

            // 3. Đảm bảo chỉ có 1 thumbnail
            List<ImageEntity> allImages = postEntity.getImages();
            long thumbCount = allImages.stream().filter(ImageEntity::getIsThumbnail).count();
            if (thumbCount == 0 && !allImages.isEmpty()) {
                allImages.get(0).setIsThumbnail(true);
            } else if (thumbCount > 1) {
                ImageEntity lastThumb = null;
                for (ImageEntity img : allImages) {
                    if (img.getIsThumbnail()) lastThumb = img;
                }
                for (ImageEntity img : allImages) {
                    img.setIsThumbnail(img == lastThumb);
                }
            }
        }

        // ==== B4. LƯU DỮ LIỆU VÀ TRẢ VỀ ====
        postEntity = postRepository.save(postEntity);
        return postMapper.toPostResponse(postEntity);
    }


    @PreAuthorize("hasRole('AUTHOR')")
    public void deletePost(Long postId) {
        // Lấy post
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        if (postEntity.getImages() != null) {
            for (ImageEntity image : postEntity.getImages()) {
                if (image.getPublicId() != null) {
                    cloudinaryService.deleteImage(image.getPublicId());
                }
            }
        }
        if (postEntity.getPublicVideoId() != null) {
            cloudinaryService.deleteVideo(postEntity.getPublicVideoId());
        }
        try {
            postRepository.deleteById(postEntity.getId());
        } catch (DataIntegrityViolationException ex) {
            // Trả lỗi về FE
//            throw new AppException(ErrorCode.POST_HAS_COMMENTS);
        }
    }

    public Page<PostResponse> getPostByOrganizationId(Long OrganizationId, SearchListPostRequest request) {
        Specification<PostEntity> spec = Specification.allOf(
                PostSpecification.hasOrganizationId(OrganizationId),
                PostSpecification.hasTitle(request.getPostTitle()),
                PostSpecification.hasStatus(request.getStatus()),
                PostSpecification.hasCreatedAt(request.getCreatedAt())
        );
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);

        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<PostEntity> page = postRepository.findAll(spec, pageable);
        return page.map(post -> {
            PostResponse response = postMapper.toPostResponse(post);
            response.setPayouts(mapPayouts(post));  // map tay
            return response;
        });

    }
    private List<PayoutResponse> mapPayouts(PostEntity post) {
        if (post.getPayouts() == null) return List.of();

        return post.getPayouts().stream().map(p -> {
            PayoutResponse res = new PayoutResponse();
            res.setId(p.getId());
            res.setAmount(p.getAmount());
            res.setAdminTransferAmount(p.getAdminTransferAmount());
            res.setNote(p.getNote());
            res.setCreatedByAdminAt(p.getCreatedByAdminAt());
            res.setConfirmedAt(p.getConfirmedAt());
            res.setPostId(p.getId());
            res.setNoteAdmin(p.getNoteAdmin());
            res.setTransferProofImageUrl(p.getTransferProofImageUrl());
            res.setTransferProofImagePublicId(p.getTransferProofImagePublicId());
            res.setRequestedAt(p.getRequestedAt());
            res.setStatus(p.getStatus());
            res.setStatusName(PayoutStatus.fromCode(p.getStatus()).getLabel());
            res.setType(p.getType());
            if (p.getRequestedBy() != null) {
                res.setRequestedBy(p.getRequestedBy().getId());
            }
            if(p.getCreatedByAdmin() != null) {
                res.setCreatedByAdmin(p.getCreatedByAdmin().getId());
            }
            res.setPostUpdate(postUpdateMapper.toPostUpdateResponse(p.getPostUpdate()));
            return res;
        }).toList();
    }

    public Page<PostResponse> getPosts(SearchListPostRequest request) {
        Specification<PostEntity> spec = Specification.allOf(
                        PostSpecification.hasOrganizationId(request.getOrganizationId()))
                .and(PostSpecification.hasTitle(request.getPostTitle()))
                .and(PostSpecification.hasCategory(request.getCategoryId()))
                .and(PostSpecification.hasCreatedAt(request.getCreatedAt()))
                .and(PostSpecification.hasEndDate(request.getEndDate()))
                .and(PostSpecification.hasStatus(PostStatus.ACTIVE.getCode()))
                .and(PostSpecification.sortAmounts(request.getTypeSort()))
                .and(PostSpecification.randomOrder(request.isRandom()));
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize()
        );

        Page<PostEntity> page = postRepository.findAll(spec, pageable);
        return page.map(postMapper::toPostResponse);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public Page<PostResponse> getPostsAdmin(SearchListPostRequest request) {
        Specification<PostEntity> spec = Specification.allOf(
                        PostSpecification.hasOrganizationId(request.getOrganizationId()))
                .and(PostSpecification.hasTitle(request.getPostTitle()))
                .and(PostSpecification.hasCategory(request.getCategoryId()))
                .and(PostSpecification.hasCreatedAt(request.getCreatedAt()))
                .and(PostSpecification.hasEndDate(request.getEndDate()))
                .and(PostSpecification.hasStatus(request.getStatus())
                .and(PostSpecification.sortAmounts(request.getTypeSort()))
                .and(PostSpecification.randomOrder(request.isRandom())));
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<PostEntity> page = postRepository.findAll(spec, pageable);
        return page.map(postMapper::toPostResponse);
    }
    @Transactional
    public PostResponse getPostById(Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // lock row để tránh concurrency issues
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        boolean liked = likeRepository.existsByPostIdAndUserId( post.getId(),user.getId());
        postViewService.addView(user.getId(), post);
        PostResponse res = postMapper.toPostResponse(post);
        res.setLiked(liked);
        res.setPayouts(mapPayouts(post));
        return res;
    }

    public Page<PostResponse> getPostsByCategory(Long categoryId, SearchListPostRequest request) {
        Specification<PostEntity> spec = Specification.allOf(
                PostSpecification.hasCategory(categoryId),
                PostSpecification.hasTitle(request.getPostTitle()),
                PostSpecification.hasCreatedAt(request.getCreatedAt())
        );
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("title").ascending()
        );

        Page<PostEntity> page = postRepository.findAll(spec, pageable);
        return page.map(postMapper::toPostResponse);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void reviewPost(Long postId, ReviewPostRequest request) {
        // 1. Tìm bài viết
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        Long currentStatus = post.getStatus();
        PostStatus targetStatus;

        try {
            targetStatus = PostStatus.fromCode(request.getStatus());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái đích không hợp lệ.");
        }

        // 2. Validate Luồng trạng thái (State Transition)
        if (targetStatus == PostStatus.ACTIVE) {
            // Chỉ được duyệt khi đang Chờ, hoặc khôi phục khi bị Chặn/Từ chối
            if (!currentStatus.equals(PostStatus.PENDING.getCode()) &&
                    !currentStatus.equals(PostStatus.REJECTED.getCode()) &&
                    !currentStatus.equals(PostStatus.BLOCKED.getCode())) {
                throw new RuntimeException("Bài viết này không ở trạng thái có thể Duyệt/Khôi phục.");
            }
        }
        else if (targetStatus == PostStatus.REJECTED) {
            // Chỉ được Từ chối khi đang Chờ duyệt
            if (!currentStatus.equals(PostStatus.PENDING.getCode())) {
                throw new RuntimeException("Chỉ có thể TỪ CHỐI bài viết đang chờ duyệt.");
            }
        }
        else if (targetStatus == PostStatus.BLOCKED) {
            // Chỉ được Chặn khi đang Hoạt động
            if (!currentStatus.equals(PostStatus.ACTIVE.getCode())) {
                throw new RuntimeException("Chỉ có thể CHẶN bài viết đang hoạt động.");
            }
        }
        else {
            throw new RuntimeException("Hành động không hợp lệ.");
        }

        // 3. Xử lý Lý do (Reason) cho cả REJECTED (90) và BLOCKED (91)
        if (targetStatus == PostStatus.REJECTED || targetStatus == PostStatus.BLOCKED) {
            if (request.getReason() == null || request.getReason().trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập lý do " + targetStatus.getLabel());
            }
            post.setReason(request.getReason());
        } else {
            // Nếu duyệt lại thì xóa lý do cũ
            post.setReason(null);
        }

        // 4. Cập nhật
        post.setStatus(targetStatus.getCode());
        post.setStatusName(targetStatus.getLabel());
        postRepository.save(post);
        // ========================================================================
        // 5. GỬI THÔNG BÁO (LOGIC MỚI THÊM)
        // ========================================================================
        try {
            // A. Lấy thông tin Admin đang thực hiện (Người gửi)
            UserEntity adminSender = userService.getMyInfoReturnEntity();
            // B. Chuẩn bị nội dung thông báo dựa trên trạng thái
            String message = "";
            String notiType = "";

            if (targetStatus == PostStatus.ACTIVE) {
                message = "Bài viết '" + post.getTitle() + "' của bạn đã được duyệt và đang hiển thị công khai.";
                notiType = "POST_APPROVED";
            } else if (targetStatus == PostStatus.REJECTED) {
                message = "Bài viết '" + post.getTitle() + "' đã bị từ chối duyệt. Lý do: " + request.getReason();
                notiType = "POST_REJECTED";
            } else if (targetStatus == PostStatus.BLOCKED) {
                message = "Bài viết '" + post.getTitle() + "' đã bị chặn do vi phạm. Lý do: " + request.getReason();
                notiType = "POST_BLOCKED";
            }

            // C. Gửi thông báo
            if (!message.isEmpty()) {
                notificationService.sendNotification(
                        post.getOrganization().getUser(),      // Người nhận: Chủ bài viết (Lấy từ PostEntity)
                        adminSender,         // Người gửi: Admin
                        message,             // Nội dung
                        notiType,            // Loại thông báo
                        "/project/" + post.getId() // Link dẫn đến bài viết (Frontend handle route này)
                );
            }
        } catch (Exception e) {
            // Log lỗi nhưng không chặn transaction chính (để việc duyệt bài vẫn thành công dù gửi noti lỗi)
            System.err.println("Lỗi gửi thông báo duyệt bài: " + e.getMessage());
        }
    }
    public List<PostResponse> getRelatedPosts(Long categoryId, Long currentPostId) {
        int TOTAL_NEEDED = 3;
        Long ACTIVE_STATUS = PostStatus.ACTIVE.getCode();
        // Bước 1: Lấy tối đa 3 bài cùng Category (trừ bài hiện tại)
        List<PostEntity> relatedPosts = postRepository.findRandomByCategoryAndIdNot(
                categoryId,
                currentPostId,
                ACTIVE_STATUS,
                TOTAL_NEEDED
        );
        // Bước 2: Kiểm tra nếu chưa đủ 3 bài
        if (relatedPosts.size() < TOTAL_NEEDED) {
            int missingCount = TOTAL_NEEDED - relatedPosts.size();

            // Tạo danh sách các ID cần loại trừ (ID hiện tại + các ID đã lấy được ở Bước 1)
            List<Long> excludedIds = new ArrayList<>();
            excludedIds.add(currentPostId);
            relatedPosts.forEach(p -> excludedIds.add(p.getId()));

            // Bước 3: Lấy bù thêm các bài từ category khác (random toàn hệ thống)
            List<PostEntity> fallbackPosts = postRepository.findRandomByIdNotIn(
                    excludedIds,
                    ACTIVE_STATUS,
                    missingCount
            );

            // Gộp danh sách
            relatedPosts.addAll(fallbackPosts);
        }

        //  - Sơ đồ minh họa luồng xử lý:
        // 1. Fetch Same Cat -> 2. Check Size -> 3. If < 3 -> Fetch Other Cat (Exclude prev IDs) -> 4. Combine.

        // Bước 4: Convert sang Response
        return postMapper.toPostResponseList(relatedPosts);
    }
    // Chạy lúc 00:00 hằng ngày
    @Scheduled(cron = "0 0 0 * * *")
    public void updateExpiredStatus() {
        LocalDateTime today = LocalDate.now().atStartOfDay();
        // Lấy tất cả record có endDate < hôm nay và status != 30
        List<PostEntity> expiredList =
                postRepository.findAllByEndDateBeforeAndStatus(today, PostStatus.ACTIVE.getCode());
        expiredList.forEach(item -> {
            item.setStatus(PostStatus.INACTIVE.getCode());
            item.setStatusName(PostStatus.INACTIVE.getLabel());
        });
        postRepository.saveAll(expiredList);
        System.out.println("[CRON] Updated " + expiredList.size() + " items at 00:00");
    }
}
