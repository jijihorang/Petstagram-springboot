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

    private Long id;
    private String name;
    private String email;
    private String password;
    private String gender;
    private String bio;
    private String phone;
    private Boolean isRecommend;
    private ProfileImageDTO profileImage;

    private List<UserEntity> userEntityList;

    public UserProfileDTO(Long id, String name, String email, ProfileImageDTO profileImage, String bio, Boolean isRecommend, String phone, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.bio = bio;
        this.isRecommend = isRecommend;
        this.phone = phone;
        this.password = password;
    }

    // Entity -> DTO
    public static UserProfileDTO toDTO(UserEntity userEntity) {
        UserProfileDTO userProfileDTO = UserProfileDTO.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .password(userEntity.getPassword())
                .name(userEntity.getName())
                .gender(userEntity.getGender())
                .bio(userEntity.getBio())
                .phone(userEntity.getPhone())
                .isRecommend(userEntity.getIsRecommend())
                .build();

        if (userEntity.getProfileImage() != null) {
            userProfileDTO.setProfileImage(ProfileImageDTO.toDTO(userEntity.getProfileImage()));
        }
        return userProfileDTO;
    }
}