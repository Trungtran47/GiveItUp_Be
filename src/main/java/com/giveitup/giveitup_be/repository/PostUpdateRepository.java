package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.PostUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostUpdateRepository extends JpaRepository<PostUpdateEntity, Long> {
    List<PostUpdateEntity> findTop10ByOrderByCreatedAtDesc();
}
