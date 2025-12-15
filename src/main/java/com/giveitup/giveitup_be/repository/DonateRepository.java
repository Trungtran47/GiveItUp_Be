package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.dto.response.DonateResponse;
import com.giveitup.giveitup_be.dto.response.DonateSummary;
import com.giveitup.giveitup_be.entity.DonateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DonateRepository extends JpaRepository<DonateEntity, Long>, JpaSpecificationExecutor<DonateEntity> {

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM DonateEntity d WHERE d.post.id = :postId")
    Double sumAmountByPostId(@Param("postId") Long postId);
    @Query("""
    SELECT new com.giveitup.giveitup_be.dto.response.DonateSummary(
           d.user.id,
           SUM(d.amount),
           MAX(d.createdAt),
           d.user
    )
    FROM DonateEntity d
    WHERE d.post.id = :postId
    GROUP BY d.user.id, d.user
    ORDER BY SUM(d.amount) DESC
""")
    Page<DonateSummary> findTotalAmountByPost(
            @Param("postId") Long postId,
            Pageable pageable
    );
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM DonateEntity d WHERE d.createdAt BETWEEN :start AND :end")
    Double sumAmountBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // --- SỬA LẠI QUERY NÀY CHO SQL SERVER ---
    // Thay DATE(created_at) bằng CAST(created_at AS DATE)
    @Query(value = "SELECT CAST(created_at AS DATE) as date, SUM(amount) as total " +
            "FROM donations " +
            "WHERE created_at BETWEEN :start AND :end " +
            "GROUP BY CAST(created_at AS DATE) " +
            "ORDER BY CAST(created_at AS DATE)", nativeQuery = true)
    List<Object[]> getDailyDonationStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
