package com.petstagram.controller;

import com.petstagram.dto.UserDTO;
import com.petstagram.entity.UserEntity;
import com.petstagram.service.FollowService;
import com.petstagram.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class FollowController {
    private final FollowService followService;
    private final UserService userService;

    @PostMapping("/follow/{toUserId}")
    public ResponseEntity<String> followUser(@PathVariable Long toUserId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String fromUserEmail = authentication.getName();
            UserEntity fromUser = userService.getUserByEmail(fromUserEmail);
            UserEntity toUser = userService.getUserById(toUserId);

            followService.follow(fromUser, toUser);
            return ResponseEntity.ok("팔로우 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/unfollow/{toUserId}")
    public ResponseEntity<String> unfollowUser(@PathVariable Long toUserId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String fromUserEmail = authentication.getName();
            UserEntity fromUser = userService.getUserByEmail(fromUserEmail);
            UserEntity toUser = userService.getUserById(toUserId);

            followService.unfollow(fromUser, toUser);
            return ResponseEntity.ok("언팔로우 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteFollower/{fromUserId}")
    public ResponseEntity<String> deleteFollower(@PathVariable Long fromUserId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String toUserEmail = authentication.getName();
            UserEntity toUser = userService.getUserByEmail(toUserEmail);
            UserEntity fromUser = userService.getUserById(fromUserId);

            followService.unfollow(fromUser, toUser);
            return ResponseEntity.ok("팔로워 삭제 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 현재 로그인한 유저 팔로잉 목록
    @GetMapping("/following")
    public ResponseEntity<List<UserDTO>> getFollowings() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            UserEntity user = userService.getUserByEmail(userEmail);

            List<UserDTO> followingList = followService.getFollowingList(user);
            return ResponseEntity.ok(followingList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 현재 로그인한 유저 팔로워 목록
    @GetMapping("/followers")
    public ResponseEntity<List<UserDTO>> getFollowers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            UserEntity user = userService.getUserByEmail(userEmail);

            List<UserDTO> followerList = followService.getFollowerList(user);
            return ResponseEntity.ok(followerList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 특정 사용자의 팔로잉 목록
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UserDTO>> getFollowingsByUserId(@PathVariable Long userId) {
        try {
            UserEntity user = userService.getUserById(userId);
            List<UserDTO> followingList = followService.getFollowingList(user);
            return ResponseEntity.ok(followingList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 특정 사용자의 팔로워 목록
    @GetMapping("/{userId}/followers")
    public ResponseEntity<List<UserDTO>> getFollowersByUserId(@PathVariable Long userId) {
        try {
            UserEntity user = userService.getUserById(userId);
            List<UserDTO> followerList = followService.getFollowerList(user);
            return ResponseEntity.ok(followerList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping("/follow-status/{toUserId}")
    public ResponseEntity<Boolean> getFollowStatus(@PathVariable Long toUserId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String fromUserEmail = authentication.getName();
            UserEntity fromUser = userService.getUserByEmail(fromUserEmail);
            UserEntity toUser = userService.getUserById(toUserId);

            boolean isFollowing = followService.isFollowing(fromUser, toUser);
            return ResponseEntity.ok(isFollowing);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @GetMapping("/followersCount/{userId}")
    public ResponseEntity<Integer> getFollowersCount(@PathVariable Long userId) {
        int count = followService.getFollowersCount(userId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/followingsCount/{userId}")
    public ResponseEntity<Integer> getFollowingsCount(@PathVariable Long userId) {
        int count = followService.getFollowingsCount(userId);
        return ResponseEntity.ok(count);
    }
}
