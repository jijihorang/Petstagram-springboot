package com.petstagram.service;

import com.petstagram.dto.PostDTO;
import com.petstagram.dto.UserDTO;
import com.petstagram.entity.ImageEntity;
import com.petstagram.entity.PostEntity;
import com.petstagram.entity.PostLikeEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.NotificationRepository;
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
    private final NotificationRepository notificationRepository;

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
        postEntity.setUser(userEntity);

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
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다."));

        long likesCount = postLikeRepository.countByPost(postEntity);

        PostDTO postDTO = PostDTO.toDTO(postEntity);
        postDTO.setPostLikesCount(likesCount);

        return postDTO;
    }

    // 사용자가 작성한 모든 게시물 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getPostsByUserId(Long userId) {
        List<PostEntity> postEntityList = postRepository.findByUserId(userId);

        return postEntityList.stream().map(postEntity -> {
            PostDTO postDTO = PostDTO.toDTO(postEntity);

            long likesCount = postLikeRepository.countByPost(postEntity);
            postDTO.setPostLikesCount(likesCount);

            return postDTO;
        }).collect(Collectors.toList());
    }

    // 게시글 수정
    public PostDTO updatePost(Long postId, PostDTO postDTO, MultipartFile file, String imageUrl) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!postEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("게시글 수정 권한이 없습니다.");
        }

        postEntity.setPostContent(postDTO.getPostContent());
        postEntity.setBreed(postDTO.getBreed());

        if (file != null && !file.isEmpty()) {
            String fileName = fileUploadService.storeFile(file);
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setImageUrl(fileName);
            imageEntity.setPost(postEntity);
            postEntity.getImageList().clear();
            postEntity.getImageList().add(imageEntity);
        }
        /* 게시글 수정 시 텍스트만 변경될 때, 현재 이미지 유지를 위한 조건 */
        else if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setImageUrl(imageUrl);
            imageEntity.setPost(postEntity);
            postEntity.getImageList().clear();
            postEntity.getImageList().add(imageEntity);
        }

        postRepository.save(postEntity);

        return PostDTO.toDTO(postEntity);
    }


    // 게시글 삭제
    public void deletePost(Long postId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));

        if (!postEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("게시글 삭제 권한이 없습니다.");
        }

        // 댓글과 관련된 모든 알림을 삭제
        notificationRepository.deleteByPostId(postId);

        // 인증된 사용자가 소유자일 경우, 게시글 삭제
        postRepository.deleteById(postId);
    }


    // 게시물 좋아요 추가 또는 삭제
    @Transactional
    public void togglePostLike(Long postId) {

        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Optional<PostLikeEntity> postLikeOpt = postLikeRepository.findByPostAndUser(post, user);

        boolean isLiked;
        if (postLikeOpt.isPresent()) {
            postLikeRepository.delete(postLikeOpt.get());
            isLiked = false;
        } else {
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
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean isLiked = postLikeRepository.findByPostAndUser(post, user).isPresent();

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