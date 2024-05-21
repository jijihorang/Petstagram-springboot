package com.petstagram.service;

import com.petstagram.dto.PostDTO;
import com.petstagram.entity.ImageEntity;
import com.petstagram.entity.PostEntity;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final FileUploadService fileUploadService;

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
    public PostDTO updatePost(Long postId, PostDTO postDTO) {
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
}