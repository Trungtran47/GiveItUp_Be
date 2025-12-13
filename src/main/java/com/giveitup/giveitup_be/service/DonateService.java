package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.DonateRequest;
import com.giveitup.giveitup_be.dto.request.SearchListDonateRequest;
import com.giveitup.giveitup_be.dto.response.DonateResponse;
import com.giveitup.giveitup_be.dto.response.DonateSummary;
import com.giveitup.giveitup_be.entity.DonateEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.enums.PostStatus;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.DonateMapper;
import com.giveitup.giveitup_be.repository.DonateRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import com.giveitup.giveitup_be.specification.DonateSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DonateService {
    DonateRepository donateRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    DonateMapper  donateMapper;
//    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMIN')")
    public DonateResponse createDonate(DonateRequest request){
        DonateEntity donateEntity = donateMapper.toDonate(request);
        PostEntity postEntity = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));
        UserEntity userEntity = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        donateEntity.setPost(postEntity);
        donateEntity.setUser(userEntity);
// Lưu donate
        DonateEntity savedDonate = donateRepository.save(donateEntity);
        // Tính tổng donate của bài post
        Double totalAmount = donateRepository.sumAmountByPostId(postEntity.getId());
        if (totalAmount >= postEntity.getTargetAmount()){
            postEntity.setStatus(PostStatus.COMPlETE.getCode());
            postEntity.setStatusName(PostStatus.COMPlETE.getLabel());
        }
        postEntity.setDonatedAmount(totalAmount);
        postRepository.save(postEntity);
        return donateMapper.toDonateResponse(savedDonate);
    }
    public Page<DonateResponse> getDonate(SearchListDonateRequest request){
            Specification<DonateEntity> spec = Specification.allOf(
                    DonateSpecification.hasUserId(request.getUserId())
            );
            int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
            Pageable pageable = PageRequest.of(
                    pageIndex,
                    request.getPageSize(),
                    Sort.by("createdAt").ascending()
            );
            Page<DonateEntity> page = donateRepository.findAll(spec, pageable);
            return page.map(donateMapper::toDonateResponse);
//        }
    }
    public List<DonateResponse> getDonateByPostId(Long postId, String keyword) {
        Specification<DonateEntity> spec = Specification.allOf(
                DonateSpecification.hasPostId(postId),
                DonateSpecification.hasKeyword(keyword)
        );
        // Lấy toàn bộ danh sách donate theo postId + keyword
        List<DonateEntity> donations = donateRepository.findAll(
                spec,
                Sort.by("createdAt").descending()
        );

        // Convert sang response
        return donations.stream()
                .map(donateMapper::toDonateResponse)
                .toList();
    }

    public Page<DonateSummary> getDonateTotalAmount(Long postId, SearchListDonateRequest request) {
        int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                request.getPageSize()
//                Sort.by(Sort.Direction.DESC, "amount")
        );
        return donateRepository.findTotalAmountByPost(postId, pageable);
    }


}
