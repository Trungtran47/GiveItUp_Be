package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.request.DonateRequest;
import com.giveitup.giveitup_be.dto.request.SearchListDonateRequest;
import com.giveitup.giveitup_be.dto.response.DonateResponse;
import com.giveitup.giveitup_be.dto.response.DonateSummary;
import com.giveitup.giveitup_be.entity.DonateEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.DonateMapper;
import com.giveitup.giveitup_be.repository.DonateRepository;
import com.giveitup.giveitup_be.repository.PostRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import com.giveitup.giveitup_be.specification.DonateSpecification;
import com.giveitup.giveitup_be.specification.PostSpecification;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
        UserEntity userEntity = userRepository.findById(String.valueOf(request.getUserId()))
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        donateEntity.setPost(postEntity);
        donateEntity.setUser(userEntity);
// Lưu donate
        DonateEntity savedDonate = donateRepository.save(donateEntity);
        // Tính tổng donate của bài post
        Double totalAmount = donateRepository.sumAmountByPostId(postEntity.getId());
        postEntity.setDonatedAmount(totalAmount);
        postRepository.save(postEntity);
        return donateMapper.toDonateResponse(savedDonate);
    }
    public Page<DonateResponse> getDonate(SearchListDonateRequest request){
//        if(Boolean.TRUE.equals(request.isSortTotalAmount())){
//            // Tổng hợp theo user
//            return getDonateTotalAmount(request);
//        } else {
            Specification<DonateEntity> spec = Specification.allOf(
                    DonateSpecification.hasUserId(request.getUserId())
            );
            ;
            int pageIndex = Math.max(request.getCurrentPage() - 1, 0);
            Pageable pageable = PageRequest.of(
                    pageIndex,
                    request.getPageSize(),
                    Sort.by("donatedAt").ascending()
            );
            Page<DonateEntity> page = donateRepository.findAll(spec, pageable);
            return page.map(donateMapper::toDonateResponse);
//        }
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
