package com.petstagram.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Getter
public class NotificationDTO {
    private final Long id;
    private final Long fromUserId;
    private final Long postId;
    private final Long commentId;
    private final Long replyId;
    private final String eventType;
    private final String regTime;

    public NotificationDTO(Long id, Long fromUserId, Long postId, Long commentId, Long replyId, String eventType, LocalDateTime regTime) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.postId = postId;
        this.commentId = commentId;
        this.replyId = replyId;
        this.eventType = eventType;
        this.regTime = regTime.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}
