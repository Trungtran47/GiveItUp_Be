package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.paging.BasePagingRequest;
import com.giveitup.giveitup_be.dto.request.*;
import com.giveitup.giveitup_be.dto.response.PayoutResponse;
import com.giveitup.giveitup_be.dto.response.PayoutResponseAdmin;
import com.giveitup.giveitup_be.entity.PayoutEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.enums.PayoutStatus;
import com.giveitup.giveitup_be.enums.PayoutType;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.OrganizationMapper;
import com.giveitup.giveitup_be.mapper.PostMapper;
import com.giveitup.giveitup_be.mapper.UserMapper;
import com.giveitup.giveitup_be.repository.OrganizationRepository;
import com.giveitup.giveitup_be.repository.PayoutRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PayoutService {
    PayoutRepository payoutRepo;
    PostRepository postRepo;
    UserRepository userRepo;
    CloudinaryService cloudinaryService;
    PostMapper postMapper;
    UserMapper userMapper;
    OrganizationMapper organizationMapper;
    UserService userService;
    // -----------------------------------------------------
    // 1. AUTHOR REQUEST PAYOUT
    // -----------------------------------------------------
    public PayoutResponse authorRequestPayout(CreatePayoutRequest req) {
        UserEntity user = userService.getMyInfoReturnEntity();
        PostEntity post = postRepo.findById(req.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        if (!post.getOrganization().getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.NOT_AUTHOR);
        }
        PayoutEntity payout = PayoutEntity.builder()
                .post(post)
                .amount(req.getAmount())
                .note(req.getNote())
                .type(PayoutType.REQUEST.name())
                .status(PayoutStatus.PENDING.getCode())
                .requestedBy(user.getOrganization())
                .requestedAt(LocalDateTime.now())
                .build();

        payoutRepo.save(payout);
        return toResponse(payout);
    }
    public PayoutResponse updatePayout(UpdatePayoutRequest req, Long authorId) {

        PayoutEntity payout = payoutRepo.findById(req.getPayoutId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));

        if (!payout.getPost().getOrganization().getId().equals(authorId)) {
            throw new AppException(ErrorCode.NOT_AUTHOR);
        }

        if (!payout.getStatus().equals(PayoutStatus.PENDING.getCode())) {
            throw new AppException(ErrorCode.PAYOUT_CANNOT_UPDATE);
        }

        payout.setAmount(Double.valueOf(req.getAmount()));
        payout.setNote(req.getNote());

        payoutRepo.save(payout);

        return toResponse(payout);
    }
    public void deletePayout(Long payoutId, Long authorId) {

        PayoutEntity payout = payoutRepo.findById(payoutId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));

        // Kiểm tra chủ sở hữu
        if (!payout.getPost().getOrganization().getId().equals(authorId)) {
            throw new AppException(ErrorCode.NOT_AUTHOR);
        }

        if (!payout.getStatus().equals(PayoutStatus.PENDING.getCode())) {
            throw new AppException(ErrorCode.PAYOUT_CANNOT_UPDATE);

        }

        payoutRepo.delete(payout);
    }

    // -----------------------------------------------------
    // 2. ADMIN APPROVE / REJECT PENDING PAYOUT
    // -----------------------------------------------------
    public PayoutResponse adminProcessPayout(ProcessPayoutRequest req, Long adminId) {

        PayoutEntity payout = payoutRepo.findById(req.getPayoutId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));
        List<Long> allowUpdate = List.of(
                PayoutStatus.PENDING.getCode(),
                PayoutStatus.TRANSFERRED.getCode()
        );
        if (!allowUpdate.contains(payout.getStatus())) {
            throw new AppException(ErrorCode.PAYOUT_CANNOT_UPDATE);
        }
        UserEntity admin = userRepo.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (req.isApproved()) {
            // ADMIN CHUYỂN LUÔN
            payout.setStatus(PayoutStatus.TRANSFERRED.getCode());
            payout.setCreatedByAdmin(admin);
            payout.setCreatedByAdminAt(LocalDateTime.now());
            payout.setNoteAdmin(req.getNoteAdmin());
            payout.setAdminTransferAmount(req.getAdminTransferAmount());
            if (req.getTransferProofImage() != null && !req.getTransferProofImage().isEmpty()) {
                // Upload file mới
                Map<String, String> uploadResult = cloudinaryService.uploadImage(
                        req.getTransferProofImage(), "GiveItUp/images_payout");
                if (payout.getTransferProofImageUrl() != null) {
                    cloudinaryService.deleteFile(payout.getTransferProofImagePublicId(), true);
                }
                payout.setTransferProofImageUrl(uploadResult.get("url"));
                payout.setTransferProofImagePublicId(uploadResult.get("public_id"));
            }
        } else {
            // ADMIN TỪ CHỐI
            payout.setStatus(PayoutStatus.REJECTED.getCode());
            payout.setNoteAdmin(req.getNoteAdmin());
        }

        payoutRepo.save(payout);
        return toResponse(payout);
    }


    // -----------------------------------------------------
    // 4. AUTHOR CONFIRM RECEIVED MONEY
    // -----------------------------------------------------
    public PayoutResponse authorConfirm(Long authorId, Long payoutId) {
        PayoutEntity payout = payoutRepo.findById(payoutId)
                .orElseThrow(() -> new AppException(ErrorCode.PAYOUT_NOT_FOUND));

        if (!payout.getPost().getOrganization().getId().equals(authorId)) {
            throw new AppException(ErrorCode.NOT_AUTHOR);
        }

        if (!payout.getStatus().equals(PayoutStatus.TRANSFERRED.getCode())) {
            throw new AppException(ErrorCode.PAYOUT_CANNOT_CONFIRM);
        }

        payout.setStatus(PayoutStatus.AUTHOR_CONFIRMED.getCode());
        payout.setConfirmedAt(LocalDateTime.now());

        payoutRepo.save(payout);
        return toResponse(payout);
    }
    public PayoutResponse adminCreatePayout(AdminCreatePayoutRequest req, Long adminId) {

        PostEntity post = postRepo.findById(req.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        UserEntity admin = userRepo.findById(adminId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PayoutEntity payout = PayoutEntity.builder()
                .post(post)
                .adminTransferAmount(req.getAdminTransferAmount())
                .noteAdmin(req.getNoteAdmin())
                .type(PayoutType.ADMIN_INIT.name())
                .status(PayoutStatus.TRANSFERRED.getCode())
                .createdByAdmin(admin)
                .build();

        payoutRepo.save(payout);
        return toResponse(payout);
    }
    public Page<PayoutResponse> getPayoutsOrganizationId(Long OrganizationId, BasePagingRequest request) {

        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);

        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("requestedAt").descending()
        );

        Page<PayoutEntity> payouts = payoutRepo.findByPost_Organization_Id(OrganizationId, pageable);

        return payouts.map(this::toResponse);
    }

    public Page<PayoutResponseAdmin> getAllPayouts(BasePagingRequest request) {

        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);

        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize(),
                Sort.by("requestedAt").descending()
        );

        Page<PayoutEntity> payouts = payoutRepo.findAll(pageable);

        return payouts.map(this::toResponseAdmin);
    }


    // -----------------------------------------------------
    // MAPPER
    // -----------------------------------------------------
    private PayoutResponse toResponse(PayoutEntity p) {
        return PayoutResponse.builder()
                .id(p.getId())
                .postId(p.getPost().getId())
                .amount(p.getAmount())
                .adminTransferAmount(p.getAdminTransferAmount())
                .status(p.getStatus())
                .statusName(PayoutStatus.fromCode(p.getStatus()).getLabel())
                .type(p.getType())
                .note(p.getNote())
                .noteAdmin(p.getNoteAdmin())
                .transferProofImageUrl(p.getTransferProofImageUrl())
                .transferProofImagePublicId(p.getTransferProofImagePublicId())
                .requestedAt(p.getRequestedAt())
//                .approvedAt(p.getApprovedAt())
                .confirmedAt(p.getConfirmedAt())
                .requestedBy(p.getRequestedBy() != null ? p.getRequestedBy().getId() : null)
//                .approvedBy(p.getApprovedBy() != null ? p.getApprovedBy().getId() : null)
                .createdByAdmin(p.getCreatedByAdmin() != null ? p.getCreatedByAdmin().getId() : null)
                .build();
    }
    private PayoutResponseAdmin toResponseAdmin(PayoutEntity p) {
        return PayoutResponseAdmin.builder()
                .id(p.getId())
                .post(postMapper.toPostResponse(p.getPost()))
                .amount(p.getAmount())
                .adminTransferAmount(p.getAdminTransferAmount())
                .status(p.getStatus())
                .statusName(PayoutStatus.fromCode(p.getStatus()).getLabel())
                .type(p.getType())
                .note(p.getNote())
                .noteAdmin(p.getNoteAdmin())
                .transferProofImageUrl(p.getTransferProofImageUrl())
                .transferProofImagePublicId(p.getTransferProofImagePublicId())
                .requestedAt(p.getRequestedAt())
                .createdByAdminAt(p.getCreatedByAdminAt())
//                .approvedAt(p.getApprovedAt())
                .confirmedAt(p.getConfirmedAt())
                .requestedBy(p.getRequestedBy() != null ? organizationMapper.toOrganizationResponse(p.getRequestedBy()) : null)
//                .approvedBy(p.getApprovedBy() != null ? userMapper.toUserResponse(p.getApprovedBy()) : null)
                .createdByAdmin(p.getCreatedByAdmin() != null ? userMapper.toUserResponse(p.getCreatedByAdmin()) : null)
                .build();
    }
}
