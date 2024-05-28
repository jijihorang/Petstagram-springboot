package com.petstagram.dto;

import com.petstagram.entity.ReplyCommentEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyCommentDTO {
    private Long id; // 대댓글의 고유 식별자.
    private String replyCommentContent; // 대댓글 내용.
    private String replyCommentEmail; // 대댓글을 작성한 사용자 이메일
    private String replyCommentregTime; // 대댓글 작성 시간.

    private boolean replyCommentLiked;
    private long replyCommentLikesCount;


    // Entity -> DTO
    public static ReplyCommentDTO toDTO(ReplyCommentEntity replyCommentEntity) {
        return ReplyCommentDTO.builder()
                .id(replyCommentEntity.getId())
                .replyCommentContent(replyCommentEntity.getReplyCommentContent())
                .replyCommentEmail(replyCommentEntity.getUser().getEmail())
                .replyCommentregTime(replyCommentEntity.getRegTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}

