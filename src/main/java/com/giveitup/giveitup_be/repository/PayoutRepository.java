package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.PayoutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutRepository extends JpaRepository<PayoutEntity, Long> {
    List<PayoutEntity> findByPostId(Long postId);
    Page<PayoutEntity> findByPost_User_Id(Long userId, Pageable pageable);


}
