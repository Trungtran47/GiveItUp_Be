package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {
    List<PostEntity> findAllByEndDateBeforeAndStatusNot(LocalDateTime endDate, Long status);
}
