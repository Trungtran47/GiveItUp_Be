package com.giveitup.giveitup_be.specification;

import com.giveitup.giveitup_be.entity.DonateEntity;
import com.giveitup.giveitup_be.entity.RoleEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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
    // Filter theo postId
    public static Specification<DonateEntity> hasPostId(Long postId) {
        return (root, query, cb) ->
                postId == null ? null :
                        cb.equal(root.get("post").get("id"), postId);
    }

    // Search theo tên người donate
    public static Specification<DonateEntity> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return null;

            String pattern = "%" + keyword.trim().toLowerCase() + "%";

            Join<DonateEntity, UserEntity> user = root.join("user", JoinType.LEFT);
            Join<UserEntity, RoleEntity> role = user.join("role", JoinType.LEFT);

            // USER → search theo firstName + lastName
            Expression<String> fullName = cb.concat(
                    cb.concat(cb.lower(user.get("firstName")), " "),
                    cb.lower(user.get("lastName"))
            );

            Predicate searchUserName = cb.or(
                    cb.like(cb.lower(user.get("firstName")), pattern),
                    cb.like(cb.lower(user.get("lastName")), pattern),
                    cb.like(fullName, pattern)
            );

            // AUTHOR / Tổ chức → search theo organizationName
            Predicate searchOrganization = cb.like(
                    cb.lower(user.get("organizationName")),
                    pattern
            );

            // Nếu là USER → điều kiện search USER
            Predicate isUser = cb.equal(role.get("name"), "USER");

            // Nếu là AUTHOR → điều kiện search tổ chức
            Predicate isAuthor = cb.equal(role.get("name"), "AUTHOR");

            // Kết hợp: nếu USER thì search userName, nếu AUTHOR thì search organizationName
            return cb.or(
                    cb.and(isUser, searchUserName),
                    cb.and(isAuthor, searchOrganization)
            );
        };
    }

}
