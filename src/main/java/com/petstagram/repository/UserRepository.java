package com.petstagram.repository;

import com.petstagram.dto.ProfileImageDTO;
import com.petstagram.dto.UserProfileDTO;
import com.petstagram.entity.FollowEntity;
import com.petstagram.entity.ProfileImageEntity;
import com.petstagram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByEmail(String email);

//    Boolean existsByNickName(String name);

    Optional<UserEntity> findByEmail(String email);

    UserEntity findByName(String username);

    @Query("SELECT new com.petstagram.dto.UserProfileDTO(u.id, u.name, u.email, " +
            "new com.petstagram.dto.ProfileImageDTO(p.id, p.imageUrl, u.id)) " +
            "FROM UserEntity u " +
            "LEFT JOIN u.profileImage p")
    List<UserProfileDTO> findAllUserProfiles();


    @Query("SELECT new com.petstagram.dto.ProfileImageDTO(pi.id, pi.imageUrl, u.id) " +
            "FROM UserEntity u JOIN u.profileImage pi WHERE u.id = :userId")
    Optional<ProfileImageDTO> findProfileImageByUserId(Long userId);

    // 팔로우 기능 추가 메서드
    @Query("SELECT f FROM FollowEntity f WHERE f.fromUser.id = :userId")
    List<FollowEntity> findFollowingsByUserId(Long userId);

    @Query("SELECT f FROM FollowEntity f WHERE f.toUser.id = :userId")
    List<FollowEntity> findFollowersByUserId(Long userId);

}