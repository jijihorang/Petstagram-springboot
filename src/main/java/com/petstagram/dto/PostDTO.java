
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
    private Long id;
    private String postContent;
    private String breed;
    private Long userId;
    private String email;
    private String regTime;
    private String location;
    private List<ImageDTO> imageList;
    private List<VideoDTO> videoList;
    private List<CommentDTO> commentList;
    private List<String> hashtags;

    private boolean postLiked;
    private long postLikesCount;

    // Entity -> DTO
    public static PostDTO toDTO(PostEntity postEntity) {
        return PostDTO.builder()
                .id(postEntity.getId())
                .postContent(postEntity.getPostContent())
                .breed(postEntity.getBreed())
                .userId(postEntity.getUser().getId())
                .email(postEntity.getUser().getEmail())
                .regTime(postEntity.getRegTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .location(postEntity.getLocation())
                .imageList(postEntity.getImageList().stream()
                        .map(ImageDTO::toDTO)
                        .collect(Collectors.toList()))
                .videoList(postEntity.getVideoList().stream()
                        .map(VideoDTO::toDTO)
                        .collect(Collectors.toList()))
                .commentList(postEntity.getCommentList().stream()
                        .map(CommentDTO::toDTO)
                        .collect(Collectors.toList()))
                .hashtags(postEntity.getPostHashTags().stream()
                        .map(postHashTag -> postHashTag.getHashtag().getName())
                        .collect(Collectors.toList()))
                .build();
    }
}
