package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long>, JpaSpecificationExecutor<PostEntity> {
    List<PostEntity> findAllByEndDateBeforeAndStatus(LocalDateTime endDate, Long status);

    @Query("""
    SELECT p, l.createdAt 
    FROM PostEntity p
    JOIN p.likes l
    WHERE l.user = :user
    ORDER BY l.createdAt DESC
""")
    Page<Object[]> findPostsAndLikeTimeByUser(
            @Param("user") UserEntity user,
            Pageable pageable
    );
    @Query("""
    SELECT p, pv.updatedAt
    FROM PostEntity p
    JOIN p.postViews pv
    WHERE pv.user = :user
    ORDER BY pv.updatedAt DESC
""")
    Page<Object[]> findPostsAndPostViewsByUser(
            @Param("user") UserEntity user,
            Pageable pageable
    );

//    List<PostEntity> findByTitleContainingIgnoreCaseOrAddressContainingIgnoreCase(String titleKeyword, String addressKeyword);
}
