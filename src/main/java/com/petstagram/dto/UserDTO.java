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
public class UserDTO {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String role = "USER";
    private String gender;
    private String bio;
    private Boolean isRecommend;
    private String token;
    private String refreshToken;
    private ProfileImageDTO profileImage;
    private String phone;

    private UserEntity userEntity;
    private List<UserEntity> userEntityList;

    // Entity -> DTO
    public static UserDTO toDTO(UserEntity userEntity) {
        UserDTO userDTO = UserDTO.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .password(userEntity.getPassword())
                .gender(userEntity.getGender())
                .bio(userEntity.getBio())
                .isRecommend(userEntity.getIsRecommend())
                .phone(userEntity.getPhone())
                .build();

        if (userEntity.getProfileImage() != null) {
            userDTO.setProfileImage(ProfileImageDTO.toDTO(userEntity.getProfileImage()));
        }

        return userDTO;
    }
}