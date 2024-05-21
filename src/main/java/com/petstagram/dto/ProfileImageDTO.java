package com.petstagram.dto;

import com.petstagram.entity.ProfileImageEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileImageDTO {
    private Long id;
    private String imageUrl;
    private Long userId;

    public ProfileImageDTO() {}

    public ProfileImageDTO(Long id, String imageUrl, Long userId) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.userId = userId;
    }

    // Entity -> DTO 변환 메서드
    public static ProfileImageDTO toDTO(ProfileImageEntity profileImageEntity) {
        ProfileImageDTO profileImageDTO = new ProfileImageDTO();
        profileImageDTO.setId(profileImageEntity.getId());
        profileImageDTO.setImageUrl(profileImageEntity.getImageUrl());
        profileImageDTO.setUserId(profileImageEntity.getUser().getId());
        return profileImageDTO;
    }
}
