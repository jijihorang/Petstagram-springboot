package com.petstagram.service;

import com.petstagram.dto.UserDTO;
import com.petstagram.dto.UserProfileDTO;
import com.petstagram.entity.ProfileImageEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.UserRepository;
import com.petstagram.service.utils.JWTUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final FileUploadService fileUploadService;

    // 새로고침 토큰
    public UserDTO refreshToken(UserDTO userDTO) {
        UserDTO response = new UserDTO();

        // 토큰에서 사용자 이메일 추출
        String ourEmail = jwtUtils.extractUsername(userDTO.getToken());

        // 이메일로 사용자 정보 조회, 없으면 예외 발생
        UserEntity users = userRepository.findByEmail(ourEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. 이메일: " + ourEmail));

        // 토큰 유효성 검사 후 유효하다면 새로운 토큰 생성
        if (jwtUtils.isTokenValid(userDTO.getToken(), users)) {
            String jwt = jwtUtils.generateToken(users);
            response.setToken(jwt); // 새로운 토큰을 응답 객체에 설정
            response.setRefreshToken(userDTO.getToken());   // 기존 새로고침 토큰을 응답 객체에 설정
        }

        // 설정된 응답 객체 반환
        return response;
    }

    // 회원가입
    public UserDTO signup(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        UserEntity userEntity = UserEntity.toEntity(userDTO, bCryptPasswordEncoder);
        UserEntity user = userRepository.save(userEntity);

        return UserDTO.toDTO(user);
    }

    // 로그인
    public UserDTO login(UserDTO userDTO) {

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userDTO.getEmail(),
                        userDTO.getPassword()));

        UserEntity user = userRepository.findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. 이메일: " + userDTO.getEmail()));


        // 조회된 사용자 정보를 바탕으로 JWT 토큰 생성
        String jwt = jwtUtils.generateToken(user);

        // 비어있는 맵과 사용자 정보를 바탕으로 새로고침 토큰 생성
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        UserDTO response = UserDTO.toDTO(user);
        response.setToken(jwt);
        response.setRole(user.getRole());
        response.setRefreshToken(refreshToken);

        return response;
    }

    // 회원 비밀번호 수정
    public UserDTO updatePassword(Long userId, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            userEntity.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        }

        userRepository.save(userEntity);

        return UserDTO.toDTO(userEntity);
    }

    // 회원 Email 수정
    public UserDTO updateEmail(Long userId, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        if (userDTO.getEmail() != null && !userEntity.getEmail().equals(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (userDTO.getEmail() != null) {
            userEntity.setEmail(userDTO.getEmail());
        }

        userRepository.save(userEntity);

        return UserDTO.toDTO(userEntity);
    }

    // 회원 Name 수정
    public UserDTO updateName(Long userId, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        if (userDTO.getName() != null) {
            userEntity.setName(userDTO.getName());
        }

        userRepository.save(userEntity);

        return UserDTO.toDTO(userEntity);
    }

    // 회원 프로필 편집 ( bio, gender, isRecommend, image )
    public UserDTO editUser(Long userId, UserDTO userDTO, MultipartFile file) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        userEntity.setBio(userDTO.getBio());
        userEntity.setGender(userDTO.getGender());
        userEntity.setIsRecommend(userDTO.getIsRecommend());

        if (file != null && !file.isEmpty()) {
            String fileName = fileUploadService.storeFile(file);

            ProfileImageEntity profileImageEntity = userEntity.getProfileImage();
            if (profileImageEntity == null) {
                profileImageEntity = new ProfileImageEntity();
            }
            profileImageEntity.setImageUrl(fileName);
            profileImageEntity.setUser(userEntity);

            userEntity.setProfileImage(profileImageEntity);
        }

        userRepository.save(userEntity);

        return UserDTO.toDTO(userEntity);
    }

    // 회원탈퇴
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // 회원 정보
    @Transactional(readOnly = true)
    public UserDTO getMyInfo(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. email = " + email));
        return UserDTO.toDTO(userEntity);
    }

    // 모든 회원 정보
    @Transactional(readOnly = true)
    public List<UserProfileDTO> getAllUserProfiles() {
        return userRepository.findAllUserProfiles();
    }

    // email로 사용자 찾기
    @Transactional(readOnly = true)
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND: 사용자를 찾을 수 없습니다. 이메일: " + email));
    }

    // id로 사용자 찾기
    @Transactional(readOnly = true)
    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND: 사용자를 찾을 수 없습니다. ID: " + userId));
    }
}