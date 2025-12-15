package com.giveitup.giveitup_be.repository;

import com.giveitup.giveitup_be.entity.FollowEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);

    Optional<FollowEntity> findByFollowerAndFollowing(UserEntity follower, UserEntity following);

    List<FollowEntity> findAllByFollower(UserEntity follower); // danh sách mình đang follow

    List<FollowEntity> findAllByFollowing(UserEntity following); // danh sách đang follow mình
    Long countByFollowing(UserEntity user); // user được follow → follower

    Long countByFollower(UserEntity user);
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);
    List<FollowEntity> findTop10ByOrderByCreatedAtDesc();
}
