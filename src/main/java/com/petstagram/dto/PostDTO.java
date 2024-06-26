
package com.petstagram.dto;

import com.petstagram.entity.PostEntity;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO {
    private Long id; // 게시물 고유 식별자
    private String postContent; // 게시물 내용(텍스트, 이미지, 비디오 링크 등).
    private String breed;
    private Long userId;
    private String email; // 게시물을 작성한 사용자 email
    private String regTime;
    private List<ImageDTO> imageList;
    private List<CommentDTO> commentList;

    private boolean postLiked; // 게시물 좋아요 상태
    private long postLikesCount; // 게시물의 좋아요 수

    private String location; // 위치

    // Entity -> DTO
    public static PostDTO toDTO(PostEntity postEntity) {
        return PostDTO.builder()
                .id(postEntity.getId())
                .postContent(postEntity.getPostContent())
                .breed(postEntity.getBreed())
                .userId(postEntity.getUser().getId())
                .email(postEntity.getUser().getEmail())
                .regTime(postEntity.getRegTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .imageList(postEntity.getImageList().stream()
                        .map(ImageDTO::toDTO)
                        .collect(Collectors.toList()))
                .commentList(postEntity.getCommentList().stream()
                        .map(CommentDTO::toDTO)
                        .collect(Collectors.toList()))
                .location(postEntity.getLocation())
                .build();
    }
}
