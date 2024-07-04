package com.petstagram.service;

import com.petstagram.dto.UserDTO;
import com.petstagram.entity.FollowEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.FollowRepository;
import com.petstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void follow(UserEntity fromUser, UserEntity toUser) throws Exception {
        // 본인 follow x
        if (fromUser.equals(toUser)) {
            throw new Exception("INVALID_REQUEST: 본인 follow 할 수 없습니다");
        }

        // 중복 follow 체크 및 상태 업데이트
        FollowEntity follow = followRepository.findFollow(fromUser, toUser).orElse(null);
        if (follow != null) {
            if (Boolean.TRUE.equals(follow.getStatus())) {
                throw new Exception("FOLLOW_DUPLICATED: 이미 follow 했습니다");
            } else {
                follow.setStatus(true);
                notificationService.sendNotification(toUser.getId(), "following", fromUser.getId(), null, null, null);
                followRepository.save(follow);

                return;
            }
        }

        follow = FollowEntity.builder()
                .toUser(toUser)
                .fromUser(fromUser)
                .status(true)
                .build();

        followRepository.save(follow);
        notificationService.sendNotification(toUser.getId(), "following", fromUser.getId(), null, null, null);
    }

    public void unfollow(UserEntity fromUser, UserEntity toUser) throws Exception {
        FollowEntity follow = followRepository.findFollow(fromUser, toUser)
                .orElseThrow(() -> new Exception("FOLLOW_NOT_FOUND: 팔로우 관계가 존재하지 않습니다"));

        followRepository.delete(follow);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getFollowingList(UserEntity user) {
        List<UserEntity> followings = followRepository.findFollowingsByUser(user);
        return followings.stream()
                .map(UserDTO::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getFollowerList(UserEntity user) {
        List<UserEntity> followers = followRepository.findFollowersByUser(user);
        return followers.stream()
                .map(UserDTO::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public int getFollowersCount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return countFollowers(user);
    }

    @Transactional(readOnly = true)
    public int getFollowingsCount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return countFollowings(user);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(UserEntity fromUser, UserEntity toUser) {
        return followRepository.findFollow(fromUser, toUser)
                .map(FollowEntity::getStatus)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public int countFollowers(UserEntity user) {
        return followRepository.findFollowersByUser(user).size();
    }

    @Transactional(readOnly = true)
    public int countFollowings(UserEntity user) {
        return followRepository.findFollowingsByUser(user).size();
    }
}
