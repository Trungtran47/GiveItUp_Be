package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.SearchHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistoryEntity, Long> {
    List<SearchHistoryEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteAllByUserId(Long userId);

}
