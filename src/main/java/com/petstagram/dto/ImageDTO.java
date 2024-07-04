package com.petstagram.dto;

import com.petstagram.entity.ImageEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageDTO {
    private Long id;
    private String imageUrl;
    private Long postId;
    private Long messageId;
    private Long storyId;

    public static ImageDTO toDTO(ImageEntity imageEntity) {
        ImageDTO imageDTO = new ImageDTO();
        imageDTO.setId(imageEntity.getId());
        imageDTO.setImageUrl(imageEntity.getImageUrl());
        if (imageEntity.getPost() != null) {
            imageDTO.setPostId(imageEntity.getPost().getId());
        }
        if (imageEntity.getStory() != null) {
            imageDTO.setStoryId(imageEntity.getStory().getId());
        }
        if (imageEntity.getMessage() != null) {
            imageDTO.setMessageId(imageEntity.getMessage().getId());
        }
        return imageDTO;
    }
}