package com.petstagram.service;

import com.petstagram.dto.CommentDTO;
import com.petstagram.dto.UserDTO;
import com.petstagram.entity.*;
import com.petstagram.repository.CommentLikeRepository;
import com.petstagram.repository.CommentRepository;
import com.petstagram.repository.PostRepository;
import com.petstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    public Long writeComment(Long postId, CommentDTO commentDTO) {
        // 게시글 찾기
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 사용자 찾기
        UserEntity currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자를 찾을 수 없습니다."));

        // DTO 를 Entity 로 변환하고 게시글 정보 설정
        CommentEntity commentEntity = CommentEntity.toEntity(commentDTO);
        commentEntity.setPost(post); // 댓글이 속한 게시글 설정
        commentEntity.setUser(currentUser); // 댓글을 작성한 사용자 설정

        // 게시글에 댓글 추가
        post.addComment(commentEntity);

        // 사용자가 작성한 댓글을 사용자의 댓글 목록에 추가
        currentUser.addComment(commentEntity);

        // 댓글 저장
        CommentEntity savedEntity = commentRepository.save(commentEntity);

        // 저장된 댓들의 ID 반환
        return savedEntity.getId();
    }

    // 댓글 리스트 및 좋아요 개수 조회
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentList(Long postId) {
        List<CommentEntity> commentList = commentRepository.findByPostId(postId);
        return commentList.stream().map(commentEntity -> {
            CommentDTO commentDTO = CommentDTO.toDTO(commentEntity);

            // 특정 게시물에 대한 좋아요 개수 조회
            long likesCount = commentLikeRepository.countByComment(commentEntity);
            commentDTO.setCommentLikesCount(likesCount);

            return commentDTO;
        }).collect(Collectors.toList());
    }

    // 댓글 수정
    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        // 댓글 찾기
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!commentEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("댓글 수정 권한이 없습니다.");
        }

        // 찾은 댓글의 내용을 업데이트
        commentEntity.setCommentContent(commentDTO.getCommentContent());

        // 댓글 저장
        commentRepository.save(commentEntity);

        return CommentDTO.toDTO(commentEntity);
    }

    // 댓글 삭제
    public void deleteComment(Long commentId) {
        // 현재 인증된 사용자의 이름(또는 이메일 등의 식별 정보) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        // 댓글 ID로 댓글 찾기
        CommentEntity commentEntity = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글을 찾을 수 없습니다."));

//        // 댓글 소유자가 현재 인증된 사용자인지 확인
//        /*if (!commentEntity.getUser().getEmail().equals(username)) {
//            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
//        }*/

        // 인증된 사용자가 소유자일 경우, 댓글 삭제
        try {
            commentRepository.deleteById(commentId);
        } catch (Exception e) {
            throw new RuntimeException("댓글 삭제 중 오류 발생");
        }
    }

    // 댓글 좋아요 추가 또는 삭제
    @Transactional
    public void toggleCommentLike(Long commentId) {

        // 댓글 찾기
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 좋아요가 이미 있는지 확인
        Optional<CommentLikeEntity> commentLikeOpt  = commentLikeRepository.findByCommentAndUser(comment, user);

        if (commentLikeOpt.isPresent()) {
            // 좋아요 엔티티가 존재한다면, 상태를 false 로 설정하고 타임스탬프 업데이트
            CommentLikeEntity commentLikeEntity = commentLikeOpt.get();
            commentLikeEntity.setCommentStatus(!commentLikeEntity.isCommentStatus());
            commentLikeRepository.delete(commentLikeEntity);
        } else {
            // 좋아요가 없다면 추가
            CommentLikeEntity commentLikeEntity = new CommentLikeEntity();
            commentLikeEntity.setComment(comment);
            commentLikeEntity.setUser(user);
            commentLikeEntity.setCommentStatus(true);
            commentLikeRepository.save(commentLikeEntity);
        }
    }

    // 댓글 좋아요 상태 조회
    public CommentDTO getCommentLikeStatus(Long commentId) {
        // 댓글 찾기
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        // 현재 인증된 사용자의 이름(또는 이메일 등) 가져오기
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 해당 댓글에 대한 사용자의 좋아요 여부 확인
        boolean isLiked = commentLikeRepository.findByCommentAndUser(comment, user).isPresent();

        // 해당 댓글의 총 좋아요 수 계산
        long likeCount = commentLikeRepository.countByComment(comment);

        // CommentDTO 객체 생성 및 반환
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentLiked(isLiked);
        commentDTO.setCommentLikesCount(likeCount);
        return commentDTO;
    }

    // 댓글 좋아요 누른 사용자 리스트 조회
    @Transactional(readOnly = true)
    public List<UserDTO> getCommentLikedUsers(Long commentId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

        List<CommentLikeEntity> likes = commentLikeRepository.findByComment(comment);

        return likes.stream()
                .map(like -> UserDTO.toDTO(like.getUser()))
                .collect(Collectors.toList());
    }
}