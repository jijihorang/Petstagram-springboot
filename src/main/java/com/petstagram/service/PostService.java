package com.petstagram.service;

import com.petstagram.dto.PostDTO;
import com.petstagram.dto.UserDTO;
import com.petstagram.entity.ImageEntity;
import com.petstagram.entity.PostEntity;
import com.petstagram.entity.PostLikeEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.PostLikeRepository;
import com.petstagram.repository.PostRepository;
import com.petstagram.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final FileUploadService fileUploadService;
    private final NotificationService notificationService;

    // 게시글 리스트 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getPostList() {
        List<PostEntity> postEntityList = postRepository.findAllByOrderByIdDesc();

        return postEntityList.stream().map(postEntity -> {
            PostDTO postDTO = PostDTO.toDTO(postEntity);

            // 특정 게시물에 대한 좋아요 개수 조회
            long likesCount = postLikeRepository.countByPost(postEntity);
            postDTO.setPostLikesCount(likesCount);

            return postDTO;
        }).collect(Collectors.toList());
    }


    // 게시글 작성
    public void writePost(PostDTO dto, MultipartFile file) {
        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 현재 로그인한 사용자의 이름을 DB 에서 가져옴
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email = " + username));

        // DTO -> Entity
        PostEntity postEntity = PostEntity.toEntity(dto);

        // 게시글에 사용자 할당
        userEntity.addPost(postEntity);

        // 이미지 업로드 처리
        if (file != null && !file.isEmpty()) {
            String fileName = fileUploadService.storeFile(file);
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setImageUrl(fileName);
            imageEntity.setPost(postEntity);
            postEntity.getImageList().add(imageEntity);
        }

        // DB에 저장
        postRepository.save(postEntity);
    }

    // 게시글 상세보기 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public PostDTO readPost(Long postId) {
        // 게시글 ID로 게시물 찾기
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다."));

        // 게시물에 대한 좋아요 개수 조회
        long likesCount = postLikeRepository.countByPost(postEntity);

        // postDTO 변환 및 좋아요 개수, 댓글 개수 설정
        PostDTO postDTO = PostDTO.toDTO(postEntity);
        postDTO.setPostLikesCount(likesCount);

        return postDTO;
    }

    // 사용자가 작성한 모든 게시물 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getPostsByUserId(Long userId) {
        List<PostEntity> postEntityList = postRepository.findByUserId(userId);

        // Entity 리스트를 DTO 리스트로 변환
        return postEntityList.stream().map(postEntity -> {
            PostDTO postDTO = PostDTO.toDTO(postEntity);

            // 특정 게시물에 대한 좋아요 개수 조회
            long likesCount = postLikeRepository.countByPost(postEntity);
            postDTO.setPostLikesCount(likesCount);

            return postDTO;
        }).collect(Collectors.toList());
    }

    // 게시글 수정
    public PostDTO updatePost(Long postId, PostDTO postDTO, MultipartFile file) {
        // 게시글 ID로 게시물 찾기
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));

        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!postEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("게시글 수정 권한이 없습니다.");
        }

        // 찾은 게시글의 내용을 업데이트
        postEntity.setPostContent(postDTO.getPostContent());

        // 이미지 업로드 처리
        if (file != null && !file.isEmpty()) {
            String fileName = fileUploadService.storeFile(file);
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setImageUrl(fileName);
            imageEntity.setPost(postEntity);
            postEntity.getImageList().clear(); // 기존 이미지 리스트를 초기화합니다.
            postEntity.getImageList().add(imageEntity);
        }

        postRepository.save(postEntity);

        return PostDTO.toDTO(postEntity);
    }

    // 게시글 삭제
    public void deletePost(Long postId) {
        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 게시글 ID로 게시물 찾기
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));

        // 게시글 소유자가 현재 인증된 사용자인지 확인
        if (!postEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("게시글 삭제 권한이 없습니다.");
        }

        // 인증된 사용자가 소유자일 경우, 게시글 삭제
        postRepository.deleteById(postId);
    }

    // 게시물 좋아요 추가 또는 삭제
    @Transactional
    public void togglePostLike(Long postId) {

        // 게시물 찾기
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 좋아요가 이미 있는지 확인
        Optional<PostLikeEntity> postLikeOpt  = postLikeRepository.findByPostAndUser(post, user);

        boolean isLiked;
        if (postLikeOpt.isPresent()) {
            // 좋아요 엔티티가 존재한다면 삭제
            postLikeRepository.delete(postLikeOpt.get());
            isLiked = false;
        } else {
            // 좋아요가 없다면 추가
            PostLikeEntity postLikeEntity = new PostLikeEntity();
            postLikeEntity.setPost(post);
            postLikeEntity.setUser(user);
            postLikeRepository.save(postLikeEntity);
            isLiked = true;
        }

        if (isLiked) {
            // 좋아요를 눌렀을 때만 알림 생성 및 전송
            notificationService.sendNotification(post.getUser().getId(), "like", user.getId(), post.getId(), null);
        }
    }

    // 게시물 좋아요 상태 조회
    public PostDTO getPostLikeStatus(Long postId) {
        // 게시물 찾기
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 해당 게시물에 대한 사용자의 좋아요 여부 확인
        boolean isLiked = postLikeRepository.findByPostAndUser(post, user).isPresent();

        // 해당 게시물의 총 좋아요 수 계산
        long likeCount = postLikeRepository.countByPost(post);

        // PostDTO 객체 생성 및 반환
        PostDTO postDTO = new PostDTO();
        postDTO.setPostLiked(isLiked);
        postDTO.setPostLikesCount(likeCount);
        return postDTO;
    }

    // 게시물 좋아요를 누른 사용자 리스트 조회
    @Transactional(readOnly = true)
    public List<UserDTO> getPostLikesList(Long postId) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));

        List<PostLikeEntity> postLikeEntities = postLikeRepository.findByPost(postEntity);

        return postLikeEntities.stream()
                .map(postLikeEntity -> UserDTO.toDTO(postLikeEntity.getUser()))
                .collect(Collectors.toList());
    }
}