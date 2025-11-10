package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.PostRequest;
import com.giveitup.giveitup_be.dto.request.SearchListPostRequest;
import com.giveitup.giveitup_be.dto.request.SearchListUserRequest;
import com.giveitup.giveitup_be.dto.response.PostResponse;
import com.giveitup.giveitup_be.dto.response.UserResponse;
import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.entity.ImageEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.PostMapper;
import com.giveitup.giveitup_be.repository.CategoryRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.specification.PostSpecification;
import com.giveitup.giveitup_be.specification.UserSpecification;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostService {
    PostRepository postRepository;
    PostMapper postMapper;
    CloudinaryService cloudinaryService;
    CategoryRepository categoryRepository;
    @PreAuthorize("hasRole('AUTHOR')")
    public PostResponse createPost(PostRequest request, MultipartFile[] files, boolean thumbIndex) {
        // B1. Map request sang entity
        PostEntity postEntity = postMapper.toPost(request);

        try {
            // B2. Lưu post trước (để có ID)
            postEntity = postRepository.save(postEntity);

            // B3. Nếu có file ảnh thì upload
            if (files != null && files.length > 0) {
                List<ImageEntity> imageEntities = new ArrayList<>();

                for (MultipartFile file : files) {
                    Map<String, String> uploadResult = cloudinaryService.uploadImage(file, "GiveItUp/posts");
                    ImageEntity image = ImageEntity.builder()
                            .imageUrl(uploadResult.get("url"))
                            .publicId(uploadResult.get("public_id"))
                            .isThumbnail(thumbIndex)
                            .post(postEntity) // liên kết với post đã có ID
                            .build();
                    imageEntities.add(image);
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
    public PostResponse updatePost(Long postId, PostRequest request, MultipartFile[] files, boolean thumbIndex) {
        // B1. Lấy post hiện có
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        // B2. Cập nhật các trường cơ bản từ request
        postEntity.setTitle(request.getTitle());
        postEntity.setDescription(request.getDescription());
        postEntity.setTargetAmount(request.getTargetAmount());
        postEntity.setEndDate(request.getEndDate());
        postEntity.setStatus(request.getStatus());
        postEntity.setCategory(categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXISTED)));

        // B3. Xóa ảnh cũ (nếu có upload mới)
        if (files != null && files.length > 0) {
            // Xóa ảnh trên Cloudinary
            if (postEntity.getImages() != null) {
                for (ImageEntity oldImg : postEntity.getImages()) {
                    cloudinaryService.deleteFile(oldImg.getPublicId(), true);
                }
            }
            // Xóa trong DB
            postEntity.getImages().clear();
            // B4. Upload ảnh mới
            List<ImageEntity> newImages = new ArrayList<>();
            for (MultipartFile file : files) {
                Map<String, String> uploadResult = cloudinaryService.uploadImage(file, "GiveItUp/posts");
                ImageEntity image = ImageEntity.builder()
                        .imageUrl(uploadResult.get("url"))
                        .publicId(uploadResult.get("public_id"))
                        .isThumbnail(thumbIndex)
                        .post(postEntity)
                        .build();

                newImages.add(image);
            }

            postEntity.getImages().addAll(newImages);
        }

        // B5. Lưu lại post đã cập nhật
        try {
            postEntity = postRepository.save(postEntity);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.POST_EXISTED);
        }

        return postMapper.toPostResponse(postEntity);
    }

    @PreAuthorize("hasRole('AUTHOR')")
    public void  deletePost(Long postId) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        postRepository.deleteById(postEntity.getId());
    }
    @PreAuthorize("hasRole('AUTHOR')")
    public Page<PostResponse> getPostByUserId(Long userId, SearchListPostRequest request) {
        Specification<PostEntity> spec = Specification.allOf(
                PostSpecification.hasUserId(userId),
                PostSpecification.hasTitle(request.getTitle()),
                PostSpecification.hasCreatedAt(request.getCreatedAt())
        );
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("title").ascending()
        );

        Page<PostEntity> page = postRepository.findAll(spec, pageable);
        log.info("Found {} users", page.getTotalElements());
        return page.map(postMapper::toPostResponse);
    }
    public Page<PostResponse> getPosts(SearchListPostRequest request) {
        Specification<PostEntity> spec = Specification.allOf(
                PostSpecification.hasTitle(request.getTitle()),
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
    public PostResponse getPostById(Long postId) {
        PostEntity postEntity = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        return postMapper.toPostResponse(postEntity);
    }
}
