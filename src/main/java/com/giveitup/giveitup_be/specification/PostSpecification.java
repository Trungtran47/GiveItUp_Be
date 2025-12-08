package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.LikeEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.enums.PostStatus;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostSpecification {
    public static Specification<PostEntity> likedByUser(UserEntity user) {
        return (root, query, criteriaBuilder) -> {
            Join<PostEntity, LikeEntity> likes = root.join("likes", JoinType.INNER);
            query.distinct(true); // tránh duplicate khi join
            query.orderBy(criteriaBuilder.desc(likes.get("createdAt"))); // sort theo ngày like
            return criteriaBuilder.equal(likes.get("user"), user);
        };
    }


    // PostSpecification.java
    public static Specification<PostEntity> hasCategory(Long categoryId) {
        return (root, query, builder) ->
                categoryId == null ? null : builder.equal(root.get("category").get("id"), categoryId);
    }


    public static Specification<PostEntity> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction(); // không filter
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }
    public static Specification<PostEntity> hasStatus(Long status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<PostEntity> hasTitle(String title) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(title)) {
                return cb.conjunction(); //  luôn trả về "true"
            }
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }
    public static Specification<PostEntity> hasEndDate(LocalDate endDate) {
        if (endDate == null) return null;
        LocalDateTime start = endDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();
        return (root, query, cb) -> cb.between(root.get("endDate"), start, end);
    }
    public static Specification<PostEntity> hasCreatedAt(LocalDate date) {
        if (date == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(
                cb.function("CONVERT", Date.class, cb.literal("DATE"), root.get("createdAt")),
                java.sql.Date.valueOf(date)
        );
    }
    public static Specification<PostEntity> sortAmounts(Integer typeSort) {
        return (root, query, cb) -> {
            if (typeSort == null) {
                return cb.conjunction();
            }
            List<Order> orders = new ArrayList<>();
            switch (typeSort) {
                case 1:
                    orders.add(cb.asc(root.get("targetAmount")));
                    break;
                case 2:
                    orders.add(cb.desc(root.get("targetAmount")));
                    break;
                case 3:
                    orders.add(cb.asc(root.get("donatedAmount")));
                    break;
                case 4:
                    orders.add(cb.desc(root.get("donatedAmount")));
                    break;
                default:
                    break; // không sort
            }
            if (!orders.isEmpty()) {
                query.orderBy(orders);
            }
            return cb.conjunction();
        };
    }


    public static Specification<PostEntity> randomOrder(Boolean isRandom) {
        return (root, query, cb) -> {
            if (Boolean.TRUE.equals(isRandom)) {

                // SQL Server random: ORDER BY NEWID()
                Expression<String> newid = cb.function("NEWID", String.class);
                assert query != null;
                query.orderBy(cb.asc(newid));
            }

            return cb.conjunction();
        };
    }

    public static Specification<PostEntity> containsKeyword(String keyword) {
        return (root, query, cb) -> {

            // ---- 1. Predicate bắt buộc: chỉ lấy bài có status = 20 ----
            Predicate statusPredicate = cb.equal(root.get("status"), PostStatus.ACTIVE.getCode());

            // ---- 2. Nếu không có keyword → chỉ lọc theo status ----
            if (keyword == null || keyword.trim().isEmpty()) {
                return statusPredicate;
            }

            String like = "%" + keyword.toLowerCase() + "%";

            // Tên, địa chỉ
            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), like);
            Predicate addressPredicate = cb.like(cb.lower(root.get("address")), like);

            // JOIN User để tìm theo tên tổ chức
            Join<PostEntity, UserEntity> userJoin = root.join("user", JoinType.LEFT);
            Predicate organizationPredicate =
                    cb.like(cb.lower(userJoin.get("organizationName")), like);

            // ---- 3. Kết hợp: (title OR address OR organization) AND status = 20 ----
            Predicate keywordPredicates = cb.or(
                    titlePredicate,
                    addressPredicate,
                    organizationPredicate
            );

            return cb.and(keywordPredicates, statusPredicate);
        };
    }


}


