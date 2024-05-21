package com.petstagram.service;

import com.petstagram.entity.FollowEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;

    public String follow(UserEntity fromUser, UserEntity toUser) throws Exception {
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
                followRepository.save(follow);
                return "SUCCESS";
            }
        }

        follow = FollowEntity.builder()
                .toUser(toUser)
                .fromUser(fromUser)
                .status(true)
                .build();

        followRepository.save(follow);
        return "SUCCESS";
    }

    public String unfollow(UserEntity fromUser, UserEntity toUser) throws Exception {
        FollowEntity follow = followRepository.findFollow(fromUser, toUser)
                .orElseThrow(() -> new Exception("FOLLOW_NOT_FOUND: 팔로우 관계가 존재하지 않습니다"));

        followRepository.delete(follow);
        return "SUCCESS";
    }

    public List<UserEntity> getFollowingList(UserEntity user) {
        return followRepository.findFollowingsByUser(user);
    }

    public List<UserEntity> getFollowerList(UserEntity user) {
        return followRepository.findFollowersByUser(user);
    }

    public boolean isFollowing(UserEntity fromUser, UserEntity toUser) {
        return followRepository.findFollow(fromUser, toUser)
                .map(FollowEntity::getStatus)
                .orElse(false);
    }

    public int countFollowers(UserEntity user) {
        return followRepository.findFollowersByUser(user).size();
    }

    public int countFollowings(UserEntity user) {
        return followRepository.findFollowingsByUser(user).size();
    }
}

