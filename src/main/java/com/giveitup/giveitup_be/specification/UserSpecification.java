package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.OrganizationEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecification {

    // 1. Tìm theo Username
    public static Specification<UserEntity> hasUsername(String username) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(username)) {
                return null;
            }
            return cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%");
        };
    }

    // 2. Tìm theo SĐT
    public static Specification<UserEntity> hasPhoneNumber(String phoneNumber) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(phoneNumber)) {
                return null;
            }
            return cb.like(cb.lower(root.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%");
        };
    }

    // 3. Tìm theo Role Name
    public static Specification<UserEntity> hasRole(String roleName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(roleName)) {
                return null;
            }
            // Join bảng Role để lấy tên
            return cb.equal(root.get("role").get("name"), roleName);
        };
    }
    // 4. Tìm theo Tên tổ chức (Dùng cho Author)
    public static Specification<UserEntity> hasOrganizationName(String orgName) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(orgName)) {
                return null;
            }
            Join<UserEntity, OrganizationEntity> orgJoin = root.join("organization", JoinType.LEFT);
            // Tìm kiếm gần đúng (LIKE)
            return cb.like(cb.lower(orgJoin.get("organizationName")), "%" + orgName.toLowerCase() + "%");
        };
    }
}