package com.petstagram.entity;

import com.petstagram.dto.ProfileImageDTO;
import com.petstagram.dto.UserDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String email;
    private String name;
    private String password;
    private String role = "USER";

    // 추가
    private String gender; // 성별
    private String bio; // 사용자 소개
    private Boolean isRecommend; // 추천 여부, 기본값은 false

    // 사용자와 게시물은 일대다 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PostEntity> postList = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private ProfileImageEntity profileImage;


    // 사용자와 팔로우 일대다 관계
    @OneToMany(mappedBy = "fromUser", fetch = FetchType.LAZY)
    private List<FollowEntity> followings = new ArrayList<>();

    @OneToMany(mappedBy = "toUser", fetch = FetchType.LAZY)
    private List<FollowEntity> followers = new ArrayList<>();

    // == 연관관계 편의 메서드 == //
    // 게시물 관련 메서드
    public void addPost(PostEntity post) {
        postList.add(post);
        post.setUser(this);
    }

    // DTO -> Entity
    public static UserEntity toEntity(UserDTO userDTO, BCryptPasswordEncoder bCryptPasswordEncoder) {
        UserEntity userEntity = UserEntity.builder()
                .id(userDTO.getId())
                .email(userDTO.getEmail())
                .name(userDTO.getName())
                .password(bCryptPasswordEncoder.encode(userDTO.getPassword()))
                .role(userDTO.getRole())
                .gender(userDTO.getGender())
                .bio(userDTO.getBio())
                .isRecommend(userDTO.getIsRecommend())
                .build();

        // ProfileImageDTO가 존재하면 ProfileImageEntity로 변환하여 설정
        if (userDTO.getProfileImage() != null) {
            ProfileImageDTO profileImageDTO = userDTO.getProfileImage();
            ProfileImageEntity profileImageEntity = new ProfileImageEntity();
            profileImageEntity.setId(profileImageDTO.getId());
            profileImageEntity.setImageUrl(profileImageDTO.getImageUrl());
            profileImageEntity.setUser(userEntity);
            userEntity.setProfileImage(profileImageEntity);
        }

        return userEntity;
    }

    public void setProfileImage(ProfileImageEntity profileImage) {
        if (profileImage == null) {
            if (this.profileImage != null) {
                this.profileImage.setUser(null);
            }
        } else {
            profileImage.setUser(this);
        }
        this.profileImage = profileImage;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}