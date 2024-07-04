package com.petstagram.repository;

import com.petstagram.dto.ProfileImageDTO;
import com.petstagram.dto.UserProfileDTO;
import com.petstagram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Boolean existsByEmail(String email);

    Optional<UserEntity> findByEmail(String email);

    UserEntity findByName(String username);

    @Query("SELECT new com.petstagram.dto.UserProfileDTO(u.id, u.name, u.email, " +
            "new com.petstagram.dto.ProfileImageDTO(p.id, p.imageUrl, u.id), " +
            "u.bio, u.isRecommend, u.phone, u.password) " +
            "FROM UserEntity u " +
            "LEFT JOIN u.profileImage p")
    List<UserProfileDTO> findAllUserProfiles();


    @Query("SELECT new com.petstagram.dto.ProfileImageDTO(pi.id, pi.imageUrl, u.id) " +
            "FROM UserEntity u JOIN u.profileImage pi WHERE u.id = :userId")
    Optional<ProfileImageDTO> findProfileImageByUserId(Long userId);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.profileImage WHERE u.email = :email")
    Optional<UserEntity> findByEmailWithProfileImage(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u LEFT JOIN FETCH u.profileImage WHERE u.id = :id")
    Optional<UserEntity> findByIdWithProfileImage(@Param("id") Long id);
}