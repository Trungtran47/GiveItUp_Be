package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.CategoryEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public class PostSpecification {
    // PostSpecification.java
    public static Specification<PostEntity> hasUserId(Long id) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(String.valueOf(id))) {
                return cb.conjunction(); // ✅ luôn trả về "true"
            }
            return cb.like(cb.lower(root.get("id")), "%" + id + "%");
        };
    }

    public static Specification<PostEntity> hasTitle(String title) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(title)) {
                return cb.conjunction(); // ✅ luôn trả về "true"
            }
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }
    public static Specification<PostEntity> hasCreatedAt(LocalDateTime createdAt) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(String.valueOf(createdAt))) {
                return cb.conjunction(); // ✅ luôn trả về "true"
            }
            return cb.like(cb.lower(root.get("createdAt")), "%" + createdAt.toLocalDate() + "%");
        };
    }
}
