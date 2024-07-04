package com.petstagram.repository;

import com.petstagram.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findByUserIdOrderByRegTimeDesc(Long userId);

    // 좋아요 중복 알림 확인
    Optional<NotificationEntity> findByUserIdAndFromUserIdAndPostIdAndEventType(Long userId, Long fromUserId, Long postId, String eventType);

    // 팔로잉 중복 알림 확인
    Optional<NotificationEntity> findByUserIdAndFromUserIdAndEventType(Long userId, Long fromUserId, String eventType);

    // 댓글 중복 알림 확인
    Optional<NotificationEntity> findByUserIdAndFromUserIdAndCommentIdAndEventType(Long userId, Long fromUserId, Long commentId, String eventType);

    // 대댓글 중복 알림 확인
    Optional<NotificationEntity> findByUserIdAndFromUserIdAndReplyIdAndEventType(Long userId, Long fromUserId, Long replyId, String eventType);

    // 댓글 ID로 알림 삭제
    void deleteByCommentId(Long commentId);
    void deleteByPostId(Long postId);

    // fromUser, post, comment 엔티티를 함께 로드
    @Query("SELECT n FROM NotificationEntity n " +
            "LEFT JOIN FETCH n.fromUser " +
            "LEFT JOIN FETCH n.post " +
            "LEFT JOIN FETCH n.comment " +
            "WHERE n.user.id = :userId " +
            "ORDER BY n.regTime DESC")
    List<NotificationEntity> findByUserIdWithFetch(@Param("userId") Long userId);
}
