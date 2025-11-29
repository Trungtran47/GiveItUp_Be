package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.DonateEntity;
import com.giveitup.giveitup_be.entity.PostEntity;
import org.springframework.data.jpa.domain.Specification;

public class DonateSpecification {
    public static Specification<DonateEntity> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }
}
