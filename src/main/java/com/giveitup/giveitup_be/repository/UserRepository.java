package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);
    @Query("""
        SELECT u FROM UserEntity u
        WHERE u.status = 30
        AND u.role.name = 'AUTHOR'
        AND LOWER(CONCAT(u.firstName, ' ', u.lastName))
            LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<UserEntity> searchAuthors(@Param("keyword") String keyword);

    long count(); // Tổng user
}
