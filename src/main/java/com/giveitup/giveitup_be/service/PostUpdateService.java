package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.PostUpdateRequest;
import com.giveitup.giveitup_be.dto.response.PostUpdateResponse;
import com.giveitup.giveitup_be.entity.PayoutEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.PostUpdateEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.PostUpdateMapper;
import com.giveitup.giveitup_be.repository.PayoutRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.PostUpdateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PostUpdateService {
    PostUpdateMapper postUpdateMapper ;
    PostUpdateRepository postUpdateRepository;
    PostRepository postRepository;
    PayoutRepository payoutRepository;
    CloudinaryService cloudinaryService;
    public PostUpdateResponse createPostUpdate(Long userId,PostUpdateRequest request) {
        PostUpdateEntity postUpdateEntity = new PostUpdateEntity();
        PostEntity post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        PayoutEntity payout = payoutRepository.findById(request.getPayoutId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));
        if (!post.getOrganization().getId().equals(userId)) {
            throw new AppException(ErrorCode.NOT_AUTHOR);
        }
        if (request.getImagePostUpdate() != null && !request.getImagePostUpdate().isEmpty()) {
            // Upload file mới
            Map<String, String> uploadResult = cloudinaryService.uploadImage(
                    request.getImagePostUpdate(), "GiveItUp/images_postUpdate");
            if (payout.getTransferProofImageUrl() != null) {
                cloudinaryService.deleteFile(payout.getTransferProofImagePublicId(), true);
            }
            postUpdateEntity.setImagePostUpdateUrl(uploadResult.get("url"));
            postUpdateEntity.setImagePostUpdatePublicId(uploadResult.get("public_id"));
        }
        postUpdateEntity.setPost(post);
        postUpdateEntity.setPayout(payout);
        postUpdateEntity.setContent(request.getContent());
        return postUpdateMapper.toPostUpdateResponse(postUpdateRepository.save(postUpdateEntity));
    }
    public PostUpdateResponse updatePostUpdate(Long userId, Long postUpdateId, PostUpdateRequest request) {
        PostUpdateEntity postUpdateEntity = postUpdateRepository.findById(postUpdateId).orElseThrow(() -> new AppException(ErrorCode.POST_UPDATE_NOT_EXISTED));
        PostEntity post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        PayoutEntity payout = payoutRepository.findById(request.getPayoutId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));
        if (!post.getOrganization().getId().equals(userId)) {
            throw new AppException(ErrorCode.NOT_AUTHOR);
        }
        if (request.getImagePostUpdate() != null && !request.getImagePostUpdate().isEmpty()) {
            // Upload file mới
            Map<String, String> uploadResult = cloudinaryService.uploadImage(
                    request.getImagePostUpdate(), "GiveItUp/images_postUpdate");
            if (payout.getTransferProofImageUrl() != null) {
                cloudinaryService.deleteFile(postUpdateEntity.getImagePostUpdatePublicId(), true);
            }
            postUpdateEntity.setImagePostUpdateUrl(uploadResult.get("url"));
            postUpdateEntity.setImagePostUpdatePublicId(uploadResult.get("public_id"));
        }
        postUpdateEntity.setPost(post);
        postUpdateEntity.setPayout(payout);
        postUpdateEntity.setContent(request.getContent());
        return postUpdateMapper.toPostUpdateResponse(postUpdateRepository.save(postUpdateEntity));
    }
}
