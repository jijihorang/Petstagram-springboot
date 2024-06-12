package com.petstagram.service;

import com.petstagram.dto.BannedDTO;
import com.petstagram.entity.BannedEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.BannedRepository;
import com.petstagram.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BannedService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BannedRepository bannedRepository;

    /* 신고(차단) */
    public void reportBannedUser(BannedDTO bannedDTO) {
        UserEntity reportedUser = userRepository.findById(bannedDTO.getReportedUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reported user ID: " + bannedDTO.getReportedUserId()));

        UserEntity reporterUser = userRepository.findById(bannedDTO.getReporterUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid reporter user ID: " + bannedDTO.getReporterUserId()));

        // 중복 신고 확인
        Optional<BannedEntity> existingBannedEntity = bannedRepository.findByReportedUserAndReporterUser(reportedUser, reporterUser);
        if (existingBannedEntity.isPresent()) {
            throw new IllegalStateException("이미 해당 유저를 신고하였습니다.");
        }

        BannedEntity bannedEntity = new BannedEntity();
        bannedEntity.setReportedUser(reportedUser);
        bannedEntity.setReporterUser(reporterUser);
        bannedEntity.setReason(bannedDTO.getReason());
        bannedRepository.save(bannedEntity);
    }

    /* 신고한 유저 목록 조회 */
    public List<BannedDTO> getBannedUser(Long reporterUserId) {
        UserEntity reporterUser = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + reporterUserId));

        List<BannedEntity> bannedEntities = bannedRepository.findByReporterUser(reporterUser);
        return bannedEntities.stream()
                .map(bannedEntity -> new BannedDTO(
                        bannedEntity.getId(),
                        bannedEntity.getReportedUser().getId(),
                        bannedEntity.getReporterUser().getId(),
                        bannedEntity.getReason()))
                .collect(Collectors.toList());
    }

    /* 자신을 신고한 유저 목록 */
    public List<BannedDTO> getBannedMe(Long reportedUserId) {
        UserEntity reportedUser = userRepository.findById(reportedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + reportedUserId));
        List<BannedEntity> bannedEntities = bannedRepository.findByReportedUser(reportedUser);

        return bannedEntities.stream()
                .map(bannedEntity -> new BannedDTO(
                        bannedEntity.getId(),
                        bannedEntity.getReportedUser().getId(),
                        bannedEntity.getReporterUser().getId(),
                        bannedEntity.getReason()))
                .collect(Collectors.toList());
    }

    /* 신고(차단) 해제 */
    public void unBannedUser(Long reporterUserId, Long reportedUserId) {
        BannedEntity bannedEntity = bannedRepository.findByReporterUserIdAndReportedUserId(reporterUserId, reportedUserId);
        if (bannedEntity != null) {
            bannedRepository.delete(bannedEntity);
        }
    }

    /* 신고한 유저 ID만 가져오기 */
    public List<Long> getBannedUserIds(Long reporterUserId) {
        UserEntity reporterUser = userRepository.findById(reporterUserId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + reporterUserId));

        List<BannedEntity> bannedEntities = bannedRepository.findByReporterUser(reporterUser);
        return bannedEntities.stream()
                .map(bannedEntity -> bannedEntity.getReportedUser().getId())
                .collect(Collectors.toList());
    }
}
