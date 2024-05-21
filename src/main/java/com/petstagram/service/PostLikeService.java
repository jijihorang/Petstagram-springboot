package com.petstagram.service;

import com.petstagram.dto.PostLikeDTO;
import com.petstagram.entity.PostEntity;
import com.petstagram.entity.PostLikeEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.PostLikeRepository;
import com.petstagram.repository.PostRepository;
import com.petstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

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
        Optional<PostLikeEntity> postLikeOpt = postLikeRepository.findByPostAndUser(post, user);

        if (postLikeOpt.isPresent()) {
            // 좋아요 엔티티가 존재한다면, 데이터베이스에서 해당 엔티티 삭제
            postLikeRepository.delete(postLikeOpt.get());
        } else {
            // 좋아요가 없다면 추가
            PostLikeEntity postLikeEntity = new PostLikeEntity();
            postLikeEntity.setPost(post);
            postLikeEntity.setUser(user);
            postLikeRepository.save(postLikeEntity);
        }
    }

    // 게시물 좋아요 상태 조회
    public PostLikeDTO getPostLikeStatus(Long postId) {
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
        PostLikeDTO postLikeDTO = new PostLikeDTO();
        postLikeDTO.setLiked(isLiked);
        postLikeDTO.setLikesCount(likeCount);
        return postLikeDTO;
    }
}