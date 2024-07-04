
package com.petstagram.service;

import com.petstagram.dto.PostDTO;
import com.petstagram.dto.UserDTO;
import com.petstagram.entity.*;
import com.petstagram.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final NotificationRepository notificationRepository;
    private final HashTagRepository hashTagRepository;
    private final FileUploadService fileUploadService;
    private final NotificationService notificationService;
    private final PostHashTagRepository postHashTagRepository;

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
    @Transactional
    public void writePost(PostDTO dto, List<MultipartFile> files, List<String> hashtagNames) {
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

        // 파일 업로드 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileName = fileUploadService.storeFile(file);
                    String contentType = file.getContentType();

                    if (contentType != null) {
                        if (contentType.startsWith("image/")) {
                            ImageEntity imageEntity = new ImageEntity();
                            imageEntity.setImageUrl(fileName);
                            imageEntity.setPost(postEntity);
                            postEntity.getImageList().add(imageEntity);
                        } else if (contentType.startsWith("video/")) {
                            VideoEntity videoEntity = new VideoEntity();
                            videoEntity.setVideoUrl(fileName);
                            videoEntity.setPost(postEntity);
                            postEntity.getVideoList().add(videoEntity);
                        }
                    }
                }
            }
        }

        // 해시태그 처리 (중복 제거를 위해 Set 사용)
        if (hashtagNames != null && !hashtagNames.isEmpty()) {
            Set<String> uniqueHashtags = new HashSet<>(hashtagNames);
            System.out.println("해시태그 리스트 (중복 제거 후): " + uniqueHashtags);

            for (String name : uniqueHashtags) {
                System.out.println("해시태그 처리 중: " + name);
                HashTagEntity hashtag = hashTagRepository.findByName(name)
                        .orElseGet(() -> {
                            HashTagEntity newHashtag = new HashTagEntity();
                            newHashtag.setName(name);
                            return hashTagRepository.save(newHashtag);
                        });
                System.out.println("해시태그 저장됨: " + hashtag.getName());
                postEntity.addHashtag(hashtag);
            }
        }

        // DB에 저장
        postRepository.save(postEntity);
    }

    // 게시글 상세보기 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public PostDTO readPost(Long postId) {
        PostEntity postEntity = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다."));

        long likesCount = postLikeRepository.countByPost(postEntity);

        PostDTO postDTO = PostDTO.toDTO(postEntity);
        postDTO.setPostLikesCount(likesCount);

        return postDTO;
    }

    // 사용자가 작성한 모든 게시물 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public List<PostDTO> getPostsByUserId(Long userId) {
        List<PostEntity> postEntityList = postRepository.findByUserIdWithLikes(userId);

        return postEntityList.stream().map(postEntity -> {
            PostDTO postDTO = PostDTO.toDTO(postEntity);

            long likesCount = postLikeRepository.countByPost(postEntity);
            postDTO.setPostLikesCount(likesCount);

            return postDTO;
        }).collect(Collectors.toList());
    }

    // 게시글 수정
    public PostDTO updatePost(Long postId, PostDTO postDTO, List<MultipartFile> files, List<String> imageUrls, String videoUrl) {
        PostEntity postEntity = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 게시물을 찾을 수 없습니다. ID: " + postId));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!postEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("게시글 수정 권한이 없습니다.");
        }

        postEntity.setPostContent(postDTO.getPostContent());
        postEntity.setBreed(postDTO.getBreed());

        // 기존 이미지 URL 유지 처리
        if (imageUrls != null && !imageUrls.isEmpty()) {
            postEntity.getImageList().clear();
            for (String imageUrl : imageUrls) {
                ImageEntity imageEntity = new ImageEntity();
                imageEntity.setImageUrl(imageUrl);
                imageEntity.setPost(postEntity);
                postEntity.getImageList().add(imageEntity);
            }
        }

        // 파일 업로드 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String fileName = fileUploadService.storeFile(file);
                    String contentType = file.getContentType();

                    if (contentType != null) {
                        if (contentType.startsWith("image/")) {
                            ImageEntity imageEntity = new ImageEntity();
                            imageEntity.setImageUrl(fileName);
                            imageEntity.setPost(postEntity);
                            postEntity.getImageList().add(imageEntity);
                        } else if (contentType.startsWith("video/")) {
                            postEntity.getVideoList().clear();
                            VideoEntity videoEntity = new VideoEntity();
                            videoEntity.setVideoUrl(fileName);
                            videoEntity.setPost(postEntity);
                            postEntity.getVideoList().add(videoEntity);
                        }
                    }
                }
            }
        }

        // 비디오 파일 유지 처리
        if (videoUrl != null && !videoUrl.isEmpty()) {
            postEntity.getVideoList().clear();
            VideoEntity videoEntity = new VideoEntity();
            videoEntity.setVideoUrl(videoUrl);
            videoEntity.setPost(postEntity);
            postEntity.getVideoList().add(videoEntity);
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

        // 게시글과 관련된 해시태그 관계 삭제
        postHashTagRepository.deleteByPostId(postId);

        // 사용되지 않는 해시태그 삭제
        hashTagRepository.deleteUnusedHashTags();

    }

    // 게시물 좋아요 추가 또는 삭제
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
            notificationService.sendNotification(post.getUser().getId(), "like", user.getId(), post.getId(), null, null);
        }
    }

    // 게시물 좋아요 상태 조회
    @Transactional(readOnly = true)
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
