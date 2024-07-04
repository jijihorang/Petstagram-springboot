
package com.petstagram.dto;

import com.petstagram.entity.VideoEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoDTO {
    private Long id;
    private String videoUrl;
    private Long postId;
    private Long messageId;
    private Long storyId;

    public static VideoDTO toDTO(VideoEntity videoEntity) {
        VideoDTO videoDTO = new VideoDTO();
        videoDTO.setId(videoEntity.getId());
        videoDTO.setVideoUrl(videoEntity.getVideoUrl());
        if (videoEntity.getPost() != null) {
            videoDTO.setPostId(videoEntity.getPost().getId());
        }
        if (videoEntity.getStory() != null) {
            videoDTO.setStoryId(videoEntity.getStory().getId());
        }
        if (videoEntity.getMessage() != null) {
            videoDTO.setMessageId(videoEntity.getMessage().getId());
        }
        return videoDTO;
    }
}
