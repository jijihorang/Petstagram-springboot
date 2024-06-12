package com.petstagram.controller;

import com.petstagram.dto.BannedDTO;
import com.petstagram.entity.UserEntity;
import com.petstagram.service.BannedService;
import com.petstagram.service.FollowService;
import com.petstagram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
public class BannedController {
    @Autowired
    private BannedService bannedService;
    @Autowired
    private UserService userService;
    @Autowired
    private FollowService followService;

    @PostMapping("/banned/{userId}")
    public ResponseEntity<String> reportBanned(@PathVariable Long userId, @RequestBody BannedDTO bannedDTO, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            UserEntity reporterUser = userService.getUserByEmail(userEmail);

            bannedDTO.setReportedUserId(userId);
            bannedDTO.setReporterUserId(reporterUser.getId());

            bannedService.reportBannedUser(bannedDTO);

            UserEntity reportedUser = userService.getUserById(userId);

            // 신고자가 신고된 유저를 팔로우하고 있다면 언팔로우
            if (followService.isFollowing(reporterUser, reportedUser)) {
                followService.unfollow(reporterUser, reportedUser);
            }

            // 신고된 유저가 신고자를 팔로우하고 있다면 언팔로우
            if (followService.isFollowing(reportedUser, reporterUser)) {
                followService.unfollow(reportedUser, reporterUser);
            }

            return ResponseEntity.ok("신고가 완료되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("중복 신고: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("신고 실패: " + e.getMessage());
        }
    }

    @GetMapping("/banned-users")
    public ResponseEntity<List<BannedDTO>> getBannedUsers(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            UserEntity reporterUser = userService.getUserByEmail(userEmail);

            List<BannedDTO> bannedUserDTOs = bannedService.getBannedUser(reporterUser.getId());

            return ResponseEntity.ok(bannedUserDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/banned-me")
    public ResponseEntity<List<BannedDTO>> getBannedMe(Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            UserEntity reportedUser = userService.getUserByEmail(userEmail);

            List<BannedDTO> bannedMeDTOs = bannedService.getBannedMe(reportedUser.getId());

            return ResponseEntity.ok(bannedMeDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/unbanned/{userId}")
    public ResponseEntity<String> unBanned(@PathVariable Long userId, Authentication authentication) {
        try {
            String userEmail = authentication.getName();
            UserEntity reporterUser = userService.getUserByEmail(userEmail);

            bannedService.unBannedUser(reporterUser.getId(), userId);

            return ResponseEntity.ok("신고 해제가 완료되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("신고 해제 실패: " + e.getMessage());
        }
    }
}
