package com.petstagram.repository;

import com.petstagram.dto.ProfileImageDTO;
import com.petstagram.dto.UserProfileDTO;
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
            "new com.petstagram.dto.ProfileImageDTO(p.id, p.imageUrl, u.id), " +
            "u.bio, u.isRecommend) " +
            "FROM UserEntity u " +
            "LEFT JOIN u.profileImage p")
    List<UserProfileDTO> findAllUserProfiles();



    @Query("SELECT new com.petstagram.dto.ProfileImageDTO(pi.id, pi.imageUrl, u.id) " +
            "FROM UserEntity u JOIN u.profileImage pi WHERE u.id = :userId")
    Optional<ProfileImageDTO> findProfileImageByUserId(Long userId);
}