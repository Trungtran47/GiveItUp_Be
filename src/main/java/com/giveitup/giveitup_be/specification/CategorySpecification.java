package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.CategoryEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CategorySpecification {
    public static Specification<CategoryEntity> hasName(String categoryName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(categoryName)) {
                return cb.conjunction(); // ✅ luôn trả về "true"
            }
            return cb.like(cb.lower(root.get("categoryName")), "%" + categoryName.toLowerCase() + "%");
        };
    }
}
