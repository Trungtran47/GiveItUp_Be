package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.dto.response.DashboardResponse;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.projection.DashboardSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /* ================= CHART DAY ================= */
    @Query(value = """
    SELECT
        t.time,
        ISNULL(d.donateCount, 0)       AS donateCount,
        ISNULL(d.totalDonated, 0)      AS totalDonated,
        ISNULL(v.totalView, 0)         AS totalView,
        ISNULL(l.totalLike, 0)         AS totalLike,
        ISNULL(c.totalComment, 0)      AS totalComment
    FROM (
        VALUES ('00'),('01'),('02'),('03'),('04'),('05'),('06'),('07'),
               ('08'),('09'),('10'),('11'),('12'),('13'),('14'),('15'),
               ('16'),('17'),('18'),('19'),('20'),('21'),('22'),('23')
    ) AS t(time)
    LEFT JOIN (
        SELECT FORMAT(d.created_at,'HH') AS h, COUNT(*) AS donateCount, SUM(d.amount) AS totalDonated
        FROM donations d
        WHERE d.user_id = :authorId
          AND d.created_at BETWEEN :start AND :end
        GROUP BY FORMAT(d.created_at,'HH')
    ) d ON d.h = t.time
    LEFT JOIN (
        SELECT FORMAT(v.created_at,'HH') AS h, ISNULL(SUM(v.view_count),0) AS totalView
        FROM post_view v
        WHERE v.user_id = :authorId
          AND v.created_at BETWEEN :start AND :end
        GROUP BY FORMAT(v.created_at,'HH')
    ) v ON v.h = t.time
    LEFT JOIN (
        SELECT FORMAT(l.created_at,'HH') AS h, COUNT(*) AS totalLike
        FROM likes l
        WHERE l.user_id = :authorId
          AND l.created_at BETWEEN :start AND :end
        GROUP BY FORMAT(l.created_at,'HH')
    ) l ON l.h = t.time
    LEFT JOIN (
        SELECT FORMAT(c.created_at,'HH') AS h, COUNT(*) AS totalComment
        FROM comments c
        WHERE c.user_id = :authorId
          AND c.created_at BETWEEN :start AND :end
        GROUP BY FORMAT(c.created_at,'HH')
    ) c ON c.h = t.time
    ORDER BY t.time
""", nativeQuery = true)
    List<Map<String, Object>> chartDayByAuthor(
            @Param("authorId") Long authorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /* ================= CHART MONTH ================= */
    @Query(value = """
    WITH days AS (
        SELECT DATEADD(DAY, n.number, :start) AS day
        FROM master..spt_values n
        WHERE n.type = 'P'
          AND DATEADD(DAY, n.number, :start) <= :end
    )
    SELECT
        FORMAT(d.day,'yyyy-MM-dd') AS time,
        ISNULL(don.donateCount,0) AS donateCount,
        ISNULL(don.totalDonated,0) AS totalDonated,
        ISNULL(v.totalView,0) AS totalView,
        ISNULL(l.totalLike,0) AS totalLike,
        ISNULL(c.totalComment,0) AS totalComment
    FROM days d
    LEFT JOIN (
        SELECT CAST(created_at AS DATE) AS day, COUNT(*) AS donateCount, SUM(amount) AS totalDonated
        FROM donations
        WHERE user_id = :authorId
          AND created_at BETWEEN :start AND :end
        GROUP BY CAST(created_at AS DATE)
    ) don ON don.day = d.day
     LEFT JOIN (
         SELECT CAST(created_at AS DATE) AS day, ISNULL(SUM(view_count),0) AS totalView
         FROM post_view
         WHERE user_id = :authorId
           AND created_at BETWEEN :start AND :end
         GROUP BY CAST(created_at AS DATE)
     ) v ON v.day = d.day
    LEFT JOIN (
        SELECT CAST(created_at AS DATE) AS day, COUNT(*) AS totalLike
        FROM likes
        WHERE user_id = :authorId
          AND created_at BETWEEN :start AND :end
        GROUP BY CAST(created_at AS DATE)
    ) l ON l.day = d.day
    LEFT JOIN (
        SELECT CAST(created_at AS DATE) AS day, COUNT(*) AS totalComment
        FROM comments
        WHERE user_id = :authorId
          AND created_at BETWEEN :start AND :end
        GROUP BY CAST(created_at AS DATE)
    ) c ON c.day = d.day
    ORDER BY d.day
""", nativeQuery = true)
    List<Map<String, Object>> chartMonthByAuthor(
            @Param("authorId") Long authorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /* ================= CHART YEAR ================= */
    @Query(value = """
    WITH months AS (
        SELECT DATEFROMPARTS(YEAR(:start), n.number, 1) AS monthStart
        FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12)) AS n(number)
    )
    SELECT
        FORMAT(m.monthStart,'yyyy-MM') AS time,
        ISNULL(don.donateCount,0) AS donateCount,
        ISNULL(don.totalDonated,0) AS totalDonated,
        ISNULL(v.totalView,0) AS totalView,
        ISNULL(l.totalLike,0) AS totalLike,
        ISNULL(c.totalComment,0) AS totalComment
    FROM months m
    LEFT JOIN (
        SELECT YEAR(created_at) AS y, MONTH(created_at) AS mo, COUNT(*) AS donateCount, SUM(amount) AS totalDonated
        FROM donations
        WHERE user_id = :authorId
          AND created_at BETWEEN :start AND :end
        GROUP BY YEAR(created_at), MONTH(created_at)
    ) don ON don.y = YEAR(m.monthStart) AND don.mo = MONTH(m.monthStart)
    LEFT JOIN (
        SELECT YEAR(created_at) AS y, MONTH(created_at) AS mo, ISNULL(SUM(view_count),0) AS totalView
        FROM post_view
        WHERE user_id = :authorId
          AND created_at BETWEEN :start AND :end
        GROUP BY YEAR(created_at), MONTH(created_at)
    ) v ON v.y = YEAR(m.monthStart) AND v.mo = MONTH(m.monthStart)
    LEFT JOIN (
        SELECT YEAR(created_at) AS y, MONTH(created_at) AS mo, COUNT(*) AS totalLike
        FROM likes
        WHERE user_id = :authorId
          AND created_at BETWEEN :start AND :end
        GROUP BY YEAR(created_at), MONTH(created_at)
    ) l ON l.y = YEAR(m.monthStart) AND l.mo = MONTH(m.monthStart)
    LEFT JOIN (
        SELECT YEAR(created_at) AS y, MONTH(created_at) AS mo, COUNT(*) AS totalComment
        FROM comments
        WHERE user_id = :authorId
          AND created_at BETWEEN :start AND :end
        GROUP BY YEAR(created_at), MONTH(created_at)
    ) c ON c.y = YEAR(m.monthStart) AND c.mo = MONTH(m.monthStart)
    ORDER BY m.monthStart
""", nativeQuery = true)
    List<Map<String, Object>> chartYearByAuthor(
            @Param("authorId") Long authorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /* ================= SUMMARY ================= */
    @Query(value = """
SELECT
    /* POSTS */
    (SELECT COUNT(*)
     FROM posts p
     WHERE p.user_id = :authorId
       AND p.created_at BETWEEN :start AND :end
    ) AS totalPost,

    /* DONATIONS */
    (SELECT COUNT(*)
     FROM donations d
     WHERE d.user_id = :authorId
       AND d.created_at BETWEEN :start AND :end
    ) AS donateCount,

    (SELECT ISNULL(SUM(d.amount),0)
     FROM donations d
     WHERE d.user_id = :authorId
       AND d.created_at BETWEEN :start AND :end
    ) AS totalDonated,

    /* VIEWS - tính dựa trên viewCount */
    (SELECT ISNULL(SUM(v.view_count),0)
     FROM post_view v
     WHERE v.user_id = :authorId
       AND v.created_at BETWEEN :start AND :end
    ) AS totalView,

    /* LIKES */
    (SELECT COUNT(*)
     FROM likes l
     WHERE l.user_id = :authorId
       AND l.created_at BETWEEN :start AND :end
    ) AS totalLike,

    /* COMMENTS */
    (SELECT COUNT(*)
     FROM comments c
     WHERE c.user_id = :authorId
       AND c.created_at BETWEEN :start AND :end
    ) AS totalComment
""", nativeQuery = true)
    DashboardSummaryProjection summaryByAuthor(
            @Param("authorId") Long authorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );


}
