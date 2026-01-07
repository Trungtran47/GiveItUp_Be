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
    List<PostEntity> findTop5ByStatusOrderByDonatedAmountDesc(Long status);
    List<PostEntity> findTop3ByStatusOrderByDonatedAmountDesc(Long status);
    List<PostEntity> findAllByStatus(Long status);
    // Cú pháp SQL Server: TOP (:limit) ... ORDER BY NEWID()
    @Query(value = "SELECT TOP (:limit) * FROM posts " +
            "WHERE category_id = :categoryId " +
            "AND id != :excludedPostId " +
            "AND status = :status " +
            "ORDER BY NEWID()", nativeQuery = true)
    List<PostEntity> findRandomByCategoryAndIdNot(@Param("categoryId") Long categoryId,
                                                  @Param("excludedPostId") Long excludedPostId,
                                                  @Param("status") Long status,
                                                  @Param("limit") int limit);
    // 2. Lấy random tất cả (Fallback), loại trừ danh sách ID đã lấy
    @Query(value = "SELECT TOP (:limit) * FROM posts " +
            "WHERE id NOT IN (:excludedIds) " +
            "AND status = :status " +
            "ORDER BY NEWID()", nativeQuery = true)
    List<PostEntity> findRandomByIdNotIn(@Param("excludedIds") List<Long> excludedIds,
                                         @Param("status") Long status,
                                         @Param("limit") int limit);
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
    // Native Query cho SQL Server
    // COLLATE SQL_Latin1_General_CP1_CI_AI: Giúp so sánh A = a, ả = a
    @Query(value = """
        SELECT * FROM posts p 
        WHERE p.status = :status 
        AND p.address COLLATE SQL_Latin1_General_CP1_CI_AI LIKE CONCAT('%', :keyword, '%')
        """, nativeQuery = true)
    List<PostEntity> searchByAddress(@Param("keyword") String keyword, @Param("status") Long status);
    // Đếm bài viết theo trạng thái
    long countByStatus(Long status);

    // Đếm bài viết đã hết hạn (endDate < now)
    @Query("SELECT COUNT(p) FROM PostEntity p WHERE p.endDate < CURRENT_TIMESTAMP")
    long countExpiredPosts();

    // Lấy top bài viết donation cao nhất
    @Query("SELECT p FROM PostEntity p ORDER BY p.donatedAmount DESC")
    List<PostEntity> findTopPerformingPosts(Pageable pageable);

    // Đếm bài viết theo Category
    @Query("SELECT p.category.categoryName as name, COUNT(p) as value " +
            "FROM PostEntity p GROUP BY p.category.categoryName")
    List<Object[]> countPostsByCategory();
    /* ================= CHART QUERIES (SQL SERVER) ================= */

    // 1. Chart Day (Theo giờ 00-23)
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
            FROM donations d JOIN posts p ON d.post_id = p.id
            WHERE p.organization_id = :organizationId AND d.created_at BETWEEN :start AND :end
            GROUP BY FORMAT(d.created_at,'HH')
        ) d ON d.h = t.time
        LEFT JOIN (
            SELECT FORMAT(v.created_at,'HH') AS h, ISNULL(SUM(v.view_count),0) AS totalView
            FROM post_view v JOIN posts p ON v.post_id = p.id
            WHERE p.organization_id = :organizationId AND v.created_at BETWEEN :start AND :end
            GROUP BY FORMAT(v.created_at,'HH')
        ) v ON v.h = t.time
        LEFT JOIN (
            SELECT FORMAT(l.created_at,'HH') AS h, COUNT(*) AS totalLike
            FROM likes l JOIN posts p ON l.post_id = p.id
            WHERE p.organization_id = :organizationId AND l.created_at BETWEEN :start AND :end
            GROUP BY FORMAT(l.created_at,'HH')
        ) l ON l.h = t.time
        LEFT JOIN (
            SELECT FORMAT(c.created_at,'HH') AS h, COUNT(*) AS totalComment
            FROM comments c JOIN posts p ON c.post_id = p.id
            WHERE p.organization_id = :organizationId AND c.created_at BETWEEN :start AND :end
            GROUP BY FORMAT(c.created_at,'HH')
        ) c ON c.h = t.time
        ORDER BY t.time
    """, nativeQuery = true)
    List<Map<String, Object>> chartDayByAuthor(@Param("organizationId") Long organizationId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);

    // 2. Chart Month (Theo ngày trong tháng)
    @Query(value = """
        WITH days AS (
            SELECT DATEADD(DAY, n.number, :start) AS day
            FROM master..spt_values n
            WHERE n.type = 'P' AND DATEADD(DAY, n.number, :start) <= :end
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
            SELECT CAST(d.created_at AS DATE) AS day, COUNT(*) AS donateCount, SUM(d.amount) AS totalDonated
            FROM donations d JOIN posts p ON d.post_id = p.id
            WHERE p.organization_id = :organizationId AND d.created_at BETWEEN :start AND :end
            GROUP BY CAST(d.created_at AS DATE)
        ) don ON don.day = d.day
        LEFT JOIN (
             SELECT CAST(v.created_at AS DATE) AS day, ISNULL(SUM(v.view_count),0) AS totalView
             FROM post_view v JOIN posts p ON v.post_id = p.id
             WHERE p.organization_id = :organizationId AND v.created_at BETWEEN :start AND :end
             GROUP BY CAST(v.created_at AS DATE)
        ) v ON v.day = d.day
        LEFT JOIN (
            SELECT CAST(l.created_at AS DATE) AS day, COUNT(*) AS totalLike
            FROM likes l JOIN posts p ON l.post_id = p.id
            WHERE p.organization_id = :organizationId AND l.created_at BETWEEN :start AND :end
            GROUP BY CAST(l.created_at AS DATE)
        ) l ON l.day = d.day
        LEFT JOIN (
            SELECT CAST(c.created_at AS DATE) AS day, COUNT(*) AS totalComment
            FROM comments c JOIN posts p ON c.post_id = p.id
            WHERE p.organization_id = :organizationId AND c.created_at BETWEEN :start AND :end
            GROUP BY CAST(c.created_at AS DATE)
        ) c ON c.day = d.day
        ORDER BY d.day
    """, nativeQuery = true)
    List<Map<String, Object>> chartMonthByAuthor(@Param("organizationId") Long organizationId,
                                                 @Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);

    // 3. Chart Year (Theo tháng)
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
            SELECT YEAR(d.created_at) AS y, MONTH(d.created_at) AS mo, COUNT(*) AS donateCount, SUM(d.amount) AS totalDonated
            FROM donations d JOIN posts p ON d.post_id = p.id
            WHERE p.organization_id = :organizationId AND d.created_at BETWEEN :start AND :end
            GROUP BY YEAR(d.created_at), MONTH(d.created_at)
        ) don ON don.y = YEAR(m.monthStart) AND don.mo = MONTH(m.monthStart)
        LEFT JOIN (
            SELECT YEAR(v.created_at) AS y, MONTH(v.created_at) AS mo, ISNULL(SUM(v.view_count),0) AS totalView
            FROM post_view v JOIN posts p ON v.post_id = p.id
            WHERE p.organization_id = :organizationId AND v.created_at BETWEEN :start AND :end
            GROUP BY YEAR(v.created_at), MONTH(v.created_at)
        ) v ON v.y = YEAR(m.monthStart) AND v.mo = MONTH(m.monthStart)
        LEFT JOIN (
            SELECT YEAR(l.created_at) AS y, MONTH(l.created_at) AS mo, COUNT(*) AS totalLike
            FROM likes l JOIN posts p ON l.post_id = p.id
            WHERE p.organization_id = :organizationId AND l.created_at BETWEEN :start AND :end
            GROUP BY YEAR(l.created_at), MONTH(l.created_at)
        ) l ON l.y = YEAR(m.monthStart) AND l.mo = MONTH(m.monthStart)
        LEFT JOIN (
            SELECT YEAR(c.created_at) AS y, MONTH(c.created_at) AS mo, COUNT(*) AS totalComment
            FROM comments c JOIN posts p ON c.post_id = p.id
            WHERE p.organization_id = :organizationId AND c.created_at BETWEEN :start AND :end
            GROUP BY YEAR(c.created_at), MONTH(c.created_at)
        ) c ON c.y = YEAR(m.monthStart) AND c.mo = MONTH(m.monthStart)
        ORDER BY m.monthStart
    """, nativeQuery = true)
    List<Map<String, Object>> chartYearByAuthor(@Param("organizationId") Long organizationId,
                                                @Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end);

    /* ================= SUMMARY QUERY ================= */
    @Query(value = """
        SELECT
            (SELECT COUNT(*) FROM posts p 
             WHERE p.organization_id = :organizationId AND p.created_at BETWEEN :start AND :end) AS totalPost,

            (SELECT COUNT(*) FROM donations d JOIN posts p ON d.post_id = p.id 
             WHERE p.organization_id = :organizationId AND d.created_at BETWEEN :start AND :end) AS donateCount,

            (SELECT ISNULL(SUM(d.amount),0) FROM donations d JOIN posts p ON d.post_id = p.id 
             WHERE p.organization_id = :organizationId AND d.created_at BETWEEN :start AND :end) AS totalDonated,

            (SELECT ISNULL(SUM(v.view_count),0) FROM post_view v JOIN posts p ON v.post_id = p.id 
             WHERE p.organization_id = :organizationId AND v.created_at BETWEEN :start AND :end) AS totalView,

            (SELECT COUNT(*) FROM likes l JOIN posts p ON l.post_id = p.id 
             WHERE p.organization_id = :organizationId AND l.created_at BETWEEN :start AND :end) AS totalLike,

            (SELECT COUNT(*) FROM comments c JOIN posts p ON c.post_id = p.id 
             WHERE p.organization_id = :organizationId AND c.created_at BETWEEN :start AND :end) AS totalComment
    """, nativeQuery = true)
    DashboardSummaryProjection summaryByAuthor(@Param("organizationId") Long organizationId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
    /* ================= CHART DAY ================= */
//    @Query(value = """
//    SELECT
//        t.time,
//        ISNULL(d.donateCount, 0)       AS donateCount,
//        ISNULL(d.totalDonated, 0)      AS totalDonated,
//        ISNULL(v.totalView, 0)         AS totalView,
//        ISNULL(l.totalLike, 0)         AS totalLike,
//        ISNULL(c.totalComment, 0)      AS totalComment
//    FROM (
//        VALUES ('00'),('01'),('02'),('03'),('04'),('05'),('06'),('07'),
//               ('08'),('09'),('10'),('11'),('12'),('13'),('14'),('15'),
//               ('16'),('17'),('18'),('19'),('20'),('21'),('22'),('23')
//    ) AS t(time)
//    LEFT JOIN (
//        SELECT FORMAT(d.created_at,'HH') AS h, COUNT(*) AS donateCount, SUM(d.amount) AS totalDonated
//        FROM donations d
//        JOIN posts p ON d.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND d.created_at BETWEEN :start AND :end
//        GROUP BY FORMAT(d.created_at,'HH')
//    ) d ON d.h = t.time
//    LEFT JOIN (
//        SELECT FORMAT(v.created_at,'HH') AS h, ISNULL(SUM(v.view_count),0) AS totalView
//        FROM post_view v
//        JOIN posts p ON v.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND v.created_at BETWEEN :start AND :end
//        GROUP BY FORMAT(v.created_at,'HH')
//    ) v ON v.h = t.time
//    LEFT JOIN (
//        SELECT FORMAT(l.created_at,'HH') AS h, COUNT(*) AS totalLike
//        FROM likes l
//        JOIN posts p ON l.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND l.created_at BETWEEN :start AND :end
//        GROUP BY FORMAT(l.created_at,'HH')
//    ) l ON l.h = t.time
//    LEFT JOIN (
//        SELECT FORMAT(c.created_at,'HH') AS h, COUNT(*) AS totalComment
//        FROM comments c
//        JOIN posts p ON c.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND c.created_at BETWEEN :start AND :end
//        GROUP BY FORMAT(c.created_at,'HH')
//    ) c ON c.h = t.time
//    ORDER BY t.time
//""", nativeQuery = true)
//    List<Map<String, Object>> chartDayByAuthor(
//            @Param("organizationId") Long organizationId,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );
//
//    /* ================= CHART MONTH ================= */
//    @Query(value = """
//    WITH days AS (
//        SELECT DATEADD(DAY, n.number, :start) AS day
//        FROM master..spt_values n
//        WHERE n.type = 'P'
//          AND DATEADD(DAY, n.number, :start) <= :end
//    )
//    SELECT
//        FORMAT(d.day,'yyyy-MM-dd') AS time,
//        ISNULL(don.donateCount,0) AS donateCount,
//        ISNULL(don.totalDonated,0) AS totalDonated,
//        ISNULL(v.totalView,0) AS totalView,
//        ISNULL(l.totalLike,0) AS totalLike,
//        ISNULL(c.totalComment,0) AS totalComment
//    FROM days d
//    LEFT JOIN (
//        SELECT CAST(d.created_at AS DATE) AS day, COUNT(*) AS donateCount, SUM(d.amount) AS totalDonated
//        FROM donations d
//        JOIN posts p ON d.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND d.created_at BETWEEN :start AND :end
//        GROUP BY CAST(d.created_at AS DATE)
//    ) don ON don.day = d.day
//     LEFT JOIN (
//         SELECT CAST(v.created_at AS DATE) AS day, ISNULL(SUM(v.view_count),0) AS totalView
//         FROM post_view v
//         JOIN posts p ON v.post_id = p.id
//         WHERE p.organization_id = :organizationId
//           AND v.created_at BETWEEN :start AND :end
//         GROUP BY CAST(v.created_at AS DATE)
//     ) v ON v.day = d.day
//    LEFT JOIN (
//        SELECT CAST(l.created_at AS DATE) AS day, COUNT(*) AS totalLike
//        FROM likes l
//        JOIN posts p ON l.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND l.created_at BETWEEN :start AND :end
//        GROUP BY CAST(l.created_at AS DATE)
//    ) l ON l.day = d.day
//    LEFT JOIN (
//        SELECT CAST(c.created_at AS DATE) AS day, COUNT(*) AS totalComment
//        FROM comments c
//        JOIN posts p ON c.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND c.created_at BETWEEN :start AND :end
//        GROUP BY CAST(c.created_at AS DATE)
//    ) c ON c.day = d.day
//    ORDER BY d.day
//""", nativeQuery = true)
//    List<Map<String, Object>> chartMonthByAuthor(
//            @Param("organizationId") Long organizationId,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );
//
//    /* ================= CHART YEAR ================= */
//    @Query(value = """
//    WITH months AS (
//        SELECT DATEFROMPARTS(YEAR(:start), n.number, 1) AS monthStart
//        FROM (VALUES (1),(2),(3),(4),(5),(6),(7),(8),(9),(10),(11),(12)) AS n(number)
//    )
//    SELECT
//        FORMAT(m.monthStart,'yyyy-MM') AS time,
//        ISNULL(don.donateCount,0) AS donateCount,
//        ISNULL(don.totalDonated,0) AS totalDonated,
//        ISNULL(v.totalView,0) AS totalView,
//        ISNULL(l.totalLike,0) AS totalLike,
//        ISNULL(c.totalComment,0) AS totalComment
//    FROM months m
//    LEFT JOIN (
//        SELECT YEAR(d.created_at) AS y, MONTH(d.created_at) AS mo, COUNT(*) AS donateCount, SUM(d.amount) AS totalDonated
//        FROM donations d
//        JOIN posts p ON d.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND d.created_at BETWEEN :start AND :end
//        GROUP BY YEAR(d.created_at), MONTH(d.created_at)
//    ) don ON don.y = YEAR(m.monthStart) AND don.mo = MONTH(m.monthStart)
//    LEFT JOIN (
//        SELECT YEAR(v.created_at) AS y, MONTH(v.created_at) AS mo, ISNULL(SUM(v.view_count),0) AS totalView
//        FROM post_view v
//        JOIN posts p ON v.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND v.created_at BETWEEN :start AND :end
//        GROUP BY YEAR(v.created_at), MONTH(v.created_at)
//    ) v ON v.y = YEAR(m.monthStart) AND v.mo = MONTH(m.monthStart)
//    LEFT JOIN (
//        SELECT YEAR(l.created_at) AS y, MONTH(l.created_at) AS mo, COUNT(*) AS totalLike
//        FROM likes l
//        JOIN posts p ON l.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND l.created_at BETWEEN :start AND :end
//        GROUP BY YEAR(l.created_at), MONTH(l.created_at)
//    ) l ON l.y = YEAR(m.monthStart) AND l.mo = MONTH(m.monthStart)
//    LEFT JOIN (
//        SELECT YEAR(c.created_at) AS y, MONTH(c.created_at) AS mo, COUNT(*) AS totalComment
//        FROM comments c
//        JOIN posts p ON c.post_id = p.id
//        WHERE p.organization_id = :organizationId
//          AND c.created_at BETWEEN :start AND :end
//        GROUP BY YEAR(c.created_at), MONTH(c.created_at)
//    ) c ON c.y = YEAR(m.monthStart) AND c.mo = MONTH(m.monthStart)
//    ORDER BY m.monthStart
//""", nativeQuery = true)
//    List<Map<String, Object>> chartYearByAuthor(
//            @Param("organizationId") Long organizationId,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );
//
//    /* ================= SUMMARY ================= */
//    @Query(value = """
//SELECT
//    /* POSTS */
//    (SELECT COUNT(*)
//     FROM posts p
//     WHERE p.organization_id = :organizationId
//       AND p.created_at BETWEEN :start AND :end
//    ) AS totalPost,
//
//    /* DONATIONS */
//    (SELECT COUNT(*)
//     FROM donations d
//     JOIN posts p ON d.post_id = p.id
//     WHERE p.organization_id = :organizationId
//       AND d.created_at BETWEEN :start AND :end
//    ) AS donateCount,
//
//    (SELECT ISNULL(SUM(d.amount),0)
//     FROM donations d
//     JOIN posts p ON d.post_id = p.id
//     WHERE p.organization_id = :organizationId
//       AND d.created_at BETWEEN :start AND :end
//    ) AS totalDonated,
//
//    /* VIEWS */
//    (SELECT ISNULL(SUM(v.view_count),0)
//     FROM post_view v
//     JOIN posts p ON v.post_id = p.id
//     WHERE p.organization_id = :organizationId
//       AND v.created_at BETWEEN :start AND :end
//    ) AS totalView,
//
//    /* LIKES */
//    (SELECT COUNT(*)
//     FROM likes l
//     JOIN posts p ON l.post_id = p.id
//     WHERE p.organization_id = :organizationId
//       AND l.created_at BETWEEN :start AND :end
//    ) AS totalLike,
//
//    /* COMMENTS */
//    (SELECT COUNT(*)
//     FROM comments c
//     JOIN posts p ON c.post_id = p.id
//     WHERE p.organization_id = :organizationId
//       AND c.created_at BETWEEN :start AND :end
//    ) AS totalComment
//""", nativeQuery = true)
//    DashboardSummaryProjection summaryByAuthor(
//            @Param("organizationId") Long organizationId,
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );





}
