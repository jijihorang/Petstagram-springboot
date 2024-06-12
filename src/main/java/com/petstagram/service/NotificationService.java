package com.petstagram.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petstagram.dto.NotificationDTO;
import com.petstagram.entity.CommentEntity;
import com.petstagram.entity.NotificationEntity;
import com.petstagram.entity.PostEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.EmitterRepository;
import com.petstagram.repository.NotificationRepository;
import com.petstagram.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmitterRepository emitterRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;

    private final ObjectMapper objectMapper;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::sendKeepAliveMessage, 0, 15, TimeUnit.SECONDS);
    }

    private void sendKeepAliveMessage() {
        emitterRepository.getAllEmitters().forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event().name("KEEP_ALIVE").data("keep-alive"));
                } catch (IOException e) {
                    emitterRepository.removeEmitter(userId, emitter);
                }
            }
        });
    }

    public SseEmitter subscribe(Long userId) {
        /* sout 다 지우기 */
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitterRepository.addEmitter(userId, emitter);

        emitter.onCompletion(() -> {
            System.out.println("Emitter completed for userId: " + userId);
            emitterRepository.removeEmitter(userId, emitter);
        });

        emitter.onTimeout(() -> {
            System.out.println("Emitter timeout for userId: " + userId);
            emitterRepository.removeEmitter(userId, emitter);
        });

        emitter.onError((e) -> {
            System.out.println("Emitter error for userId: " + userId + ", error: " + e.getMessage());
            emitterRepository.removeEmitter(userId, emitter);
        });

        try {
            emitter.send(SseEmitter.event().name("INIT").data("SSE connection established for userId: " + userId));
        } catch (IOException e) {
            System.err.println("Failed to send INIT event: " + e.getMessage());
        }

        return emitter;
    }

    public void sendNotification(Long userId, String eventType, Long fromUserId, Long postId, Long commentId) {
        UserEntity user = UserEntity.builder().id(userId).build();
        UserEntity fromUser = UserEntity.builder().id(fromUserId).build();

        if ("like".equals(eventType)) {
            handleLikeNotification(userId, fromUserId, postId, eventType);
        } else if ("following".equals(eventType)) {
            handleFollowNotification(userId, fromUserId, eventType);
        } else if ("comment".equals(eventType)) {
            handleCommentNotification(userId, fromUserId, postId, commentId, eventType);
        }
    }

    private void handleLikeNotification(Long userId, Long fromUserId, Long postId, String eventType) {
        PostEntity post = postRepository.findById(postId).orElse(null);
        if (post == null) {
            return;
        }

        Long postAuthorId = post.getAuthorId();
        if (fromUserId.equals(postAuthorId)) {
            return;
        }

        Optional<NotificationEntity> existingNotification = notificationRepository.findByUserIdAndFromUserIdAndPostIdAndEventType(userId, fromUserId, postId, eventType);
        if (existingNotification.isPresent()) {
            return;
        }

        saveNotification(userId, fromUserId, postId, eventType);

        sendSseEvent(userId, new NotificationDTO(null, fromUserId, postId, null, eventType, LocalDateTime.now()), eventType);
    }

    private void handleFollowNotification(Long userId, Long fromUserId, String eventType) {
        Optional<NotificationEntity> existingNotification = notificationRepository.findByUserIdAndFromUserIdAndEventType(userId, fromUserId, eventType);
        if (existingNotification.isPresent()) {
            return;
        }

        saveNotification(userId, fromUserId, eventType);

        sendSseEvent(userId, new NotificationDTO(null, fromUserId, null, null, eventType, LocalDateTime.now()), eventType);
    }

    private void handleCommentNotification(Long userId, Long fromUserId, Long postId, Long commentId, String eventType) {
        Optional<NotificationEntity> existingNotification = notificationRepository.findByUserIdAndFromUserIdAndCommentIdAndEventType(userId, fromUserId, commentId, eventType);
        if (userId.equals(fromUserId)) {
            return;
        }

        if (existingNotification.isPresent()) {
            return;
        }

        saveNotification(userId, fromUserId, postId, commentId, eventType);

        sendSseEvent(userId, new NotificationDTO(null, fromUserId, postId, commentId, eventType, LocalDateTime.now()), eventType);
    }

    private void sendSseEvent(Long userId, NotificationDTO notificationData, String eventType) {
        var emitters = emitterRepository.getEmitters(userId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    String jsonData = objectMapper.writeValueAsString(notificationData);
                    emitter.send(SseEmitter.event().name(eventType).data(jsonData));
                    System.out.println("Notification sent to emitter: " + jsonData);
                } catch (IOException e) {
                    System.err.println("Failed to send notification: " + e.getMessage());
                    emitterRepository.removeEmitter(userId, emitter);
                } catch (Exception e) {
                    System.err.println("Unexpected error: " + e.getMessage());
                    e.printStackTrace();
                    emitterRepository.removeEmitter(userId, emitter);
                }
            }
        }
    }

    // 좋아요 알림 save
    public void saveNotification(Long userId, Long fromUserId, Long postId, String eventType) {
        NotificationEntity notification = NotificationEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .fromUser(UserEntity.builder().id(fromUserId).build())
                .post(PostEntity.builder().id(postId).build())
                .eventType(eventType)
                .build();
        notificationRepository.save(notification);
    }

    // 팔로우 알림 save
    public void saveNotification(Long userId, Long fromUserId, String eventType) {
        NotificationEntity notification = NotificationEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .fromUser(UserEntity.builder().id(fromUserId).build())
                .eventType(eventType)
                .build();
        notificationRepository.save(notification);
    }

    // 댓글 알림 save
    public void saveNotification(Long userId, Long fromUserId, Long postId, Long commentId, String eventType) {
        NotificationEntity notification = NotificationEntity.builder()
                .user(UserEntity.builder().id(userId).build())
                .fromUser(UserEntity.builder().id(fromUserId).build())
                .post(PostEntity.builder().id(postId).build())
                .comment(CommentEntity.builder().id(commentId).build())
                .eventType(eventType)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> findByUserId(Long userId) {
        List<NotificationEntity> notifications = notificationRepository.findByUserIdOrderByRegTimeDesc(userId);
        return notifications.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private NotificationDTO convertToDto(NotificationEntity notification) {
        return new NotificationDTO(
                notification.getId(),
                notification.getFromUser().getId(),
                notification.getPost() != null ? notification.getPost().getId() : null,
                notification.getComment() != null ? notification.getComment().getId() : null,
                notification.getEventType(),
                notification.getRegTime()
        );
    }
}
