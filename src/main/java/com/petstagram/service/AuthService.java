
package com.petstagram.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.petstagram.dto.KakaoAccountDto;
import com.petstagram.dto.KakaoTokenDto;
import com.petstagram.dto.UserDTO;
import com.petstagram.entity.ProfileImageEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.UserRepository;
import com.petstagram.service.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final String KAKAO_CLIENT_ID = "3a5712f42ce2ffd6b969bbc0605ac1c2";
    private final String KAKAO_REDIRECT_URI = "http://localhost:5173/login/oauth2/callback/kakao";
    private final String KAKAO_CLIENT_SECRET = "Xl9xVVi3OhbBpeBd6KxD2TtZD1D21hWE";
    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final FileUploadService fileUploadService;

    // 카카오 로그인
    public UserDTO kakaoLogin(String code) {
        KakaoTokenDto kakaoTokenDto = getKakaoAccessToken(code);
        String accessToken = kakaoTokenDto.getAccess_token();

        // 카카오 사용자 정보 조회
        KakaoAccountDto kakaoUserInfoDto = getKakaoUserInfo(accessToken);
        String email = kakaoUserInfoDto.getKakao_account().getEmail();
        String nickname = kakaoUserInfoDto.getProperties().getNickname();
        String profileImageUrl = kakaoUserInfoDto.getProperties().getProfile_image();

        // 사용자 이메일을 사용하여 DB에서 사용자 정보 조회
        UserEntity user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 사용자 정보가 없으면 새로운 사용자 등록
                    UserEntity newUser = new UserEntity();
                    newUser.setName(nickname);
                    newUser.setEmail(email);
                    newUser.setPassword(bCryptPasswordEncoder.encode("카카오"));

                    // 프로필 이미지 설정
                    String savedFileName = null;
                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        savedFileName = downloadAndSaveProfileImage(profileImageUrl);
                    }

                    ProfileImageEntity profileImageEntity = new ProfileImageEntity();
                    profileImageEntity.setImageUrl(savedFileName);
                    profileImageEntity.setUser(newUser);
                    newUser.setProfileImage(profileImageEntity);

                    return userRepository.save(newUser);
                });

        // JWT 토큰 생성
        String jwt = jwtUtils.generateToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        UserDTO response = UserDTO.toDTO(user);
        response.setToken(jwt);
        response.setRole(user.getRole());
        response.setRefreshToken(refreshToken);

        return response;
    }

    // 카카오 액세스 토큰 발급
    public KakaoTokenDto getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", KAKAO_CLIENT_ID);
        params.add("redirect_uri", KAKAO_REDIRECT_URI);
        params.add("code", code);
        params.add("client_secret", KAKAO_CLIENT_SECRET);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        KakaoTokenDto kakaoTokenDto;
        try {
            kakaoTokenDto = objectMapper.readValue(response.getBody(), KakaoTokenDto.class);
        } catch (Exception e) {
            throw new RuntimeException("카카오 토큰을 받아오는 데 실패했습니다.");
        }

        return kakaoTokenDto;
    }

    // 카카오 사용자 정보 조회
    private KakaoAccountDto getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        KakaoAccountDto kakaoUserInfoDto;
        try {
            kakaoUserInfoDto = objectMapper.readValue(response.getBody(), KakaoAccountDto.class);
        } catch (Exception e) {
            throw new RuntimeException("카카오 사용자 정보를 받아오는 데 실패했습니다.");
        }

        return kakaoUserInfoDto;
    }

    // 프로필 이미지 다운로드 및 저장
    private String downloadAndSaveProfileImage(String profileImageUrl) {
        try {
            URL url = new URL(profileImageUrl);
            Resource resource = new UrlResource(url);
            Path tempFile = Files.createTempFile("profileImage", ".jpg");
            Files.copy(resource.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
            return fileUploadService.storeFile(tempFile.toFile());
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지를 다운로드하고 저장하는 데 실패했습니다.", e);
        }
    }
}
