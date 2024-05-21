package com.petstagram.service;

import com.petstagram.dto.CommentDTO;
import com.petstagram.entity.CommentEntity;
import com.petstagram.entity.PostEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.CommentLikeRepository;
import com.petstagram.repository.CommentRepository;
import com.petstagram.repository.PostRepository;
import com.petstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        // 댓글 소유자가 현재 인증된 사용자인지 확인
        if (!commentEntity.getUser().getEmail().equals(username)) {
            throw new IllegalStateException("댓글 삭제 권한이 없습니다.");
        }

        // 인증된 사용자가 소유자일 경우, 댓글 삭제
        commentRepository.deleteById(commentId);
    }
}