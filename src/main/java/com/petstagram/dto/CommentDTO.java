package com.petstagram.dto;

import com.petstagram.entity.CommentEntity;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long id;
    private String commentContent;
    private String commentEmail;
    private String commentRegTime;
    private boolean commentLiked;
    private long commentLikesCount;
    private List<ReplyCommentDTO> replyCommentList;
    private Long postId;

    // Entity -> DTO
    public static CommentDTO toDTO(CommentEntity commentEntity) {
        return CommentDTO.builder()
                .id(commentEntity.getId())
                .commentContent(commentEntity.getCommentContent())
                .commentEmail(commentEntity.getUser().getEmail())
                .commentRegTime(commentEntity.getRegTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .replyCommentList(commentEntity.getReplyCommentList().stream()
                        .map(ReplyCommentDTO::toDTO)
                        .collect(Collectors.toList()))
                .postId(commentEntity.getPost().getId())
                .build();
    }
}