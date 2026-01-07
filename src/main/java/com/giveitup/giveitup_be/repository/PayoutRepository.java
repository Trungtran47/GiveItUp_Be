package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.PayoutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PayoutRepository extends JpaRepository<PayoutEntity, Long> {
    List<PayoutEntity> findByPostId(Long postId);
    Page<PayoutEntity> findByPost_Organization_Id(Long orgId, Pageable pageable);

//    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PayoutEntity p WHERE p.status = :status AND p.updatedAt BETWEEN :start AND :end")
//    Double sumPayoutByStatusBetween(@Param("status") Long status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PayoutEntity p " +
            "WHERE p.status IN :statuses AND p.updatedAt BETWEEN :start AND :end")
    Double sumPayoutByStatusesBetween(@Param("statuses") List<Long> statuses,
                                      @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
    List<PayoutEntity> findByStatus(Long status);

    // --- SỬA LẠI QUERY NÀY CHO SQL SERVER ---
    // Thay DATE(updated_at) bằng CAST(updated_at AS DATE)
    @Query(value = "SELECT CAST(updated_at AS DATE) as date, SUM(amount) as total " +
            "FROM payouts " +
            "WHERE status IN (:statuses) AND updated_at BETWEEN :start AND :end " + // 1. Sửa thành IN (:statuses)
            "GROUP BY CAST(updated_at AS DATE) " +
            "ORDER BY CAST(updated_at AS DATE)", nativeQuery = true)
    List<Object[]> getDailyPayoutStats(@Param("statuses") List<Long> statuses, // 2. Sửa thành List<Long>
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}