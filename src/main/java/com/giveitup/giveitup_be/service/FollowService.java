package com.giveitup.giveitup_be.service;

import com.giveitup.giveitup_be.dto.response.FollowResponse;
import com.giveitup.giveitup_be.entity.FollowEntity;
import com.giveitup.giveitup_be.entity.UserEntity;
import com.giveitup.giveitup_be.exception.AppException;
import com.giveitup.giveitup_be.exception.ErrorCode;
import com.giveitup.giveitup_be.mapper.FollowMapper;
import com.giveitup.giveitup_be.repository.FollowRepository;
import com.giveitup.giveitup_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;

    // FOLLOW
    public String follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Không thể tự follow chính mình");
        }

        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        if (exists) {
            throw new RuntimeException("Đã follow rồi");
        }

        FollowEntity entity = FollowEntity.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(entity);

        return "Follow thành công";
    }

    // UNFOLLOW
    public String unfollow(Long followerId, Long followingId) {
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        FollowEntity follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        followRepository.delete(follow);

        return "Unfollow thành công";
    }

    // LIST FOLLOWING
    public List<FollowResponse> getFollowing(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return followRepository.findAllByFollower(user)
                .stream()
                .map(f -> followMapper.toFollowResponse(f.getFollowing()))
                .toList();
    }

    // LIST FOLLOWERS
    public List<FollowResponse> getFollowers(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return followRepository.findAllByFollowing(user)
                .stream()
                .map(f -> followMapper.toFollowResponse(f.getFollower()))
                .toList();
    }

    // COUNT FOLLOWING
    public long countFollowing(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return followRepository.findAllByFollower(user).size();
    }

    // COUNT FOLLOWERS
    public long countFollowers(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return followRepository.findAllByFollowing(user).size();
    }

    // CHECK ALREADY FOLLOWED?
    public boolean isFollowing(Long me, Long target) {
        UserEntity follower = userRepository.findById(me)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        UserEntity following = userRepository.findById(target)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return followRepository.existsByFollowerAndFollowing(follower, following);
    }
}
