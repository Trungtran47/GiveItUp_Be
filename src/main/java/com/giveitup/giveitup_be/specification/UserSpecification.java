package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    public static Specification<UserEntity> hasUsername(String username) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(username)) {
                return cb.conjunction(); // ✅ luôn trả về "true"
            }
            return cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
        };
    }

    public static Specification<UserEntity> hasPhoneNumber(String phoneNumber) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(phoneNumber)) {
                return cb.conjunction(); // ✅ luôn "true"
            }
            return cb.like(cb.lower(root.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%");
        };
    }
}
