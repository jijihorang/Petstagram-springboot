package com.petstagram.dto;

import com.petstagram.entity.StoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryDTO {
    private Long id;
    private String storyText;
    private List<ImageDTO> imageList;
    private List<VideoDTO> videoList;
    private String storyType;
    private UserDTO user;
    private String regTime;

    // Entity -> DTO
    public static StoryDTO toDTO(StoryEntity storyEntity) {
        return StoryDTO.builder()
                .id(storyEntity.getId())
                .storyType(storyEntity.getStoryType())
                .imageList(storyEntity.getImageList().stream()
                        .map(ImageDTO::toDTO)
                        .collect(Collectors.toList()))
                .videoList(storyEntity.getVideoList().stream()
                        .map(VideoDTO::toDTO)
                        .collect(Collectors.toList()))
                .user(UserDTO.toDTO(storyEntity.getUser()))
                .regTime(storyEntity.getRegTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
}