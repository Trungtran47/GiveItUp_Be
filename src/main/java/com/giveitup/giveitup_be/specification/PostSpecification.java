package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PostSpecification {
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

    public static Specification<PostEntity> hasTitle(String title) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(title)) {
                return cb.conjunction(); //  luôn trả về "true"
            }
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }
    public static Specification<PostEntity> hasCreatedAt(LocalDateTime date) {
        if (date == null) {
            return null; // hoặc return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(
                cb.function("DATE", Date.class, root.get("createdAt")),
                java.sql.Date.valueOf(String.valueOf(date))
        );
    }
    public static Specification<PostEntity> hasEndDate(LocalDateTime endDate) {
        if (endDate == null) {
            return null; // hoặc return (root, query, cb) -> cb.conjunction();
        }
        return (root, query, cb) -> cb.equal(
                cb.function("DATE", Date.class, root.get("endDate")),
                java.sql.Date.valueOf(String.valueOf(endDate))
        );
    }
    public static Specification<PostEntity> sortAmounts(Boolean sortTargetAmount, Boolean sortDonatedAmount) {
        return (root, query, cb) -> {
            List<Order> orders = new ArrayList<>();

            if (sortTargetAmount != null) {
                orders.add(sortTargetAmount ? cb.desc(root.get("targetAmount")) : cb.asc(root.get("targetAmount")));
            }

            if (sortDonatedAmount != null) {
                orders.add(sortDonatedAmount ? cb.desc(root.get("donatedAmount")) : cb.asc(root.get("donatedAmount")));
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



}
