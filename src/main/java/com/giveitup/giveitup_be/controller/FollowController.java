package com.giveitup.giveitup_be.controller;

import com.giveitup.giveitup_be.dto.response.FollowResponse;
import com.giveitup.giveitup_be.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{me}/follow/{target}")
    public String follow(@PathVariable Long me, @PathVariable Long target) {
        return followService.follow(me, target);
    }

    @DeleteMapping("/{me}/unfollow/{target}")
    public String unfollow(@PathVariable Long me, @PathVariable Long target) {
        return followService.unfollow(me, target);
    }

    @GetMapping("/{userId}/following")
    public List<FollowResponse> getFollowing(@PathVariable Long userId) {
        return followService.getFollowing(userId);
    }

    @GetMapping("/{userId}/followers")
    public List<FollowResponse> getFollowers(@PathVariable Long userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/{userId}/following/count")
    public long countFollowing(@PathVariable Long userId) {
        return followService.countFollowing(userId);
    }

    @GetMapping("/{userId}/followers/count")
    public long countFollowers(@PathVariable Long userId) {
        return followService.countFollowers(userId);
    }

    @GetMapping("/{me}/is-following/{target}")
    public boolean isFollowing(@PathVariable Long me, @PathVariable Long target) {
        return followService.isFollowing(me, target);
    }
}
