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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowMapper followMapper;
    private final NotificationService notificationService;

    // FOLLOW
    public String toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Không thể tự follow chính mình");
        }
        UserEntity follower = userRepository.findById(followerId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        UserEntity following = userRepository.findById(followingId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        // Kiểm tra đã follow chưa
        Optional<FollowEntity> existingFollow =
                followRepository.findByFollowerAndFollowing(follower, following);
        // Nếu có rồi → unfollow
        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
            return "Unfollow thành công";
        }

        // Nếu chưa có → follow
        FollowEntity follow = FollowEntity.builder()
                .follower(follower)
                .following(following)
                .build();
        followRepository.save(follow);

        String displayName;
        if (follower.getOrganization() != null) {
            displayName = follower.getOrganization().getOrganizationName();
        }
        // 2. Nếu là User thường -> Lấy firstName + lastName
        else {
            String firstName = follower.getFirstName() == null ? "" : follower.getFirstName();
            String lastName = follower.getLastName() == null ? "" : follower.getLastName();
            displayName = (firstName + " " + lastName).trim();
            if (displayName.isEmpty()) {
                displayName = follower.getUsername();
            }
        }
        notificationService.sendNotification(
                following,                          // Người nhận (người được follow)
                follower,                           // Người gửi (người đi follow)
                displayName + " đã theo dõi bạn.",  // Nội dung message đã xử lý tên
                "FOLLOW",                           // Loại thông báo
                "/u/" + follower.getId()      // Link dẫn tới trang cá nhân người đó
        );
        return "Follow thành công";
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
