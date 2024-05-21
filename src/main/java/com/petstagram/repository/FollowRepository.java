package com.petstagram.repository;

import com.petstagram.entity.FollowEntity;
import com.petstagram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<FollowEntity, Long> {

    @Query("SELECT f FROM FollowEntity f WHERE f.fromUser = :from AND f.toUser = :to")
    Optional<FollowEntity> findFollow(@Param("from") UserEntity fromUser, @Param("to") UserEntity toUser);

    @Query("SELECT f.toUser FROM FollowEntity f WHERE f.fromUser = :fromUser")
    List<UserEntity> findFollowingsByUser(UserEntity fromUser);

    @Query("SELECT f.fromUser FROM FollowEntity f WHERE f.toUser = :toUser")
    List<UserEntity> findFollowersByUser(UserEntity toUser);
}
