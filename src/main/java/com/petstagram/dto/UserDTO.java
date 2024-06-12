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

    private Long id;    // 사용자의 고유 식별자
    private String name;    // 사용자의 이름
    private String email;   // 사용자의 이메일
    private String password;    // 사용자의 비밀번호
    private String role = "USER";   // 사용자의 역할
    private String gender; // 성별
    private String bio; // 사용자 소개
    private Boolean isRecommend; // 추천 여부, 기본값은 false
    private String token;   // 사용자의 세션 또는 인증을 확인하기 위해 사용되는 JWT
    private String refreshToken;    // token 이 만료되었을 때, 새로운 token 을 발급받기 위해 사용되는 토큰
    private ProfileImageDTO profileImage;

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
                .build();

        if (userEntity.getProfileImage() != null) {
            userDTO.setProfileImage(ProfileImageDTO.toDTO(userEntity.getProfileImage()));
        }

        return userDTO;
    }
}