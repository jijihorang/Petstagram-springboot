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
    private final FollowService followService;

    // 회원가입
    public UserDTO signup(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // UserDTO 를 UserEntity 로 변환
        UserEntity userEntity = UserEntity.toEntity(userDTO, bCryptPasswordEncoder);

        // 사용자 등록
        UserEntity user = userRepository.save(userEntity);

        // 저장된 사용자 정보를 다시 DTO 로 변환하여 반환
        return UserDTO.toDTO(user);
    }

    // 로그인
    public UserDTO login(UserDTO userDTO) {

        // 전달 받은 이메일과 비밀번호를 인증 처리
        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(userDTO.getEmail(),
                        userDTO.getPassword()));

        // 이메일로 사용자 조회 없으면 예외 발생
        UserEntity user = userRepository.findByEmail(userDTO.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. 이메일: " + userDTO.getEmail()));


        // 조회된 사용자 정보를 바탕으로 JWT 토큰 생성
        String jwt = jwtUtils.generateToken(user);

        // 비어있는 맵과 사용자 정보를 바탕으로 새로고침 토큰 생성
        String refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);

        // 응답 객체에 토큰, 사용자 역할, 새로고침 토큰 설정
        UserDTO response = UserDTO.toDTO(user);
        response.setToken(jwt);
        response.setRole(user.getRole());
        response.setRefreshToken(refreshToken);

        // 설정된 응답 객체 반환
        return response;
    }

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

    // 회원수정
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다. ID: " + userId));

        // 현재 사용자의 이메일과 수정하려는 이메일이 다르면서, 수정하려는 이메일이 이미 사용 중인지 확인
        if (!userEntity.getEmail().equals(userDTO.getEmail()) && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        userEntity.setEmail(userDTO.getEmail());
        userEntity.setName(userDTO.getName());

        // userDTO 의 비밀번호가 null 이 아니고, 비어있지 않은 경우에만 업데이트
        // 새 비밀번호는 bCryptPasswordEncoder 를 사용하여 암호화
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            userEntity.setPassword(bCryptPasswordEncoder.encode(userDTO.getPassword()));
        }

        // 변경된 사용자 정보를 userRepository 를 통해 저장
        userRepository.save(userEntity);

        // 업데이트된 사용자 엔티티를 UserDTO 로 변환하여 반환
        return UserDTO.toDTO(userEntity);
    }

    // 회원 프로필 편집 ( bio, gender, image ) 추가
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

        userRepository.save(userEntity); // 변경 사항을 저장

        return UserDTO.toDTO(userEntity);
    }

    // 회원탈퇴
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // 회원 마이페이지
    public UserDTO getMyInfo(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. email = " + email));
        return UserDTO.toDTO(userEntity);
    }

    // 모든 회원의 id, email, name, image 정보
    public List<UserProfileDTO> getAllUserProfiles() {
        return userRepository.findAllUserProfiles();
    }

    public UserDTO getUsersById(Long userId) {
        UserDTO userDTO = new UserDTO();
        UserEntity usersById = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User Not found"));
        userDTO.setUserEntity(usersById);
        return userDTO;
    }

    // 팔로우 UserId 찾기
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND: 사용자를 찾을 수 없습니다. 이메일: " + email));
    }

    public UserEntity getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND: 사용자를 찾을 수 없습니다. ID: " + userId));
    }

    // 팔로워 갯수 증가 메소드 추가
    public int getFollowersCount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return followService.countFollowers(user);
    }

    // 팔로잉 갯수 증가 메소드 추가
    public int getFollowingsCount(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        return followService.countFollowings(user);
    }
}