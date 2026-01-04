package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.LikeEntity;
import com.giveitup.giveitup_be.entity.OrganizationEntity;
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


    public static Specification<PostEntity> hasOrganizationId(Long OrganizationId) {
        return (root, query, cb) -> {
            if (OrganizationId == null) {
                return cb.conjunction(); // không filter
            }
            return cb.equal(root.get("organization").get("id"), OrganizationId);
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

            // --- THAY ĐỔI Ở ĐÂY ---
            // Thay vì cb.equal, ta tạo mệnh đề IN
            CriteriaBuilder.In<Long> inStatus = cb.in(root.get("status"));

            // Thêm các status bạn muốn vào đây
            inStatus.value(PostStatus.ACTIVE.getCode());
            inStatus.value(PostStatus.INACTIVE.getCode()); // Ví dụ status thứ 2
            inStatus.value(PostStatus.COMPlETE.getCode());   // Ví dụ status thứ 3

            // Gán lại vào biến statusPredicate để logic bên dưới không cần sửa nhiều
            Predicate statusPredicate = inStatus;
            // ---------------------

            if (keyword == null || keyword.trim().isEmpty()) {
                return statusPredicate;
            }

            String like = "%" + keyword.toLowerCase() + "%";

            Predicate titlePredicate = cb.like(cb.lower(root.get("title")), like);
            Predicate addressPredicate = cb.like(cb.lower(root.get("address")), like);

            // Join organization
            Join<PostEntity, OrganizationEntity> orgJoin = root.join("organization", JoinType.LEFT);
            Predicate organizationPredicate = cb.like(cb.lower(orgJoin.get("organizationName")), like);

            Predicate keywordPredicate = cb.or(titlePredicate, addressPredicate, organizationPredicate);

            return cb.and(statusPredicate, keywordPredicate);
        };
    }


}


