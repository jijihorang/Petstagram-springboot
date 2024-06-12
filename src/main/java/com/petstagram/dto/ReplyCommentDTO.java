package com.petstagram.dto;

import com.petstagram.entity.ReplyCommentEntity;
import lombok.*;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReplyCommentDTO {
    private Long id;
    private String replyCommentContent;
    private String replyCommentEmail;
    private String replyCommentRegTime;

    private boolean replyCommentLiked;
    private long replyCommentLikesCount;


    // Entity -> DTO
    public static ReplyCommentDTO toDTO(ReplyCommentEntity replyCommentEntity) {
        return ReplyCommentDTO.builder()
                .id(replyCommentEntity.getId())
                .replyCommentContent(replyCommentEntity.getReplyCommentContent())
                .replyCommentEmail(replyCommentEntity.getUser().getEmail())
                .replyCommentRegTime(replyCommentEntity.getRegTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}