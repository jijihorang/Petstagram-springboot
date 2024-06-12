package com.petstagram.repository;

import com.petstagram.entity.BannedEntity;
import com.petstagram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface BannedRepository extends JpaRepository<BannedEntity, Long> {
    Optional<BannedEntity> findByReportedUserAndReporterUser(UserEntity reportedUser, UserEntity reporterUser);

    List<BannedEntity> findByReporterUser(UserEntity reporterUser);
    List<BannedEntity> findByReportedUser(UserEntity reportedUser);

    BannedEntity findByReporterUserIdAndReportedUserId(Long reporterUserId, Long reportedUserId);

}


