package com.petstagram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.petstagram.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Long id;    // 사용자의 고유 식별자
    private String name;    // 사용자의 이름
    private String email;   // 사용자의 이메일

    private String gender; // 성별
    private String bio; // 사용자 소개
    private Boolean isRecommend; // 추천 여부, 기본값은 false
    private ProfileImageDTO profileImage;

    private List<UserEntity> userEntityList;

    public UserProfileDTO(Long id, String name, String email, ProfileImageDTO profileImage) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
    }

    // Entity -> DTO
    public static UserProfileDTO toDTO(UserEntity userEntity) {
        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .gender(userEntity.getGender())
                .bio(userEntity.getBio())
                .isRecommend(userEntity.getIsRecommend())
                .build();

        if (userEntity.getProfileImage() != null) {
            userProfileDTO.setProfileImage(ProfileImageDTO.toDTO(userEntity.getProfileImage()));
        }
        return userProfileDTO;
    }
}