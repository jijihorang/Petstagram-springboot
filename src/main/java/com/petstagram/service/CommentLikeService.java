package com.petstagram.service;

import com.petstagram.entity.CommentEntity;
import com.petstagram.entity.CommentLikeEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.CommentLikeRepository;
import com.petstagram.repository.CommentRepository;
import com.petstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 댓글 좋아요 추가
    public void addCommentLike(Long commentId, Long userId) {

        // 댓글 찾기
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 사용자 찾기
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        CommentLikeEntity commentLikeEntity = new CommentLikeEntity();
        commentLikeEntity.setComment(comment);
        commentLikeEntity.setUser(user);
        commentLikeRepository.save(commentLikeEntity);
    }

    // 댓글 좋아요 삭제
    @Transactional
    public void removeCommentLike(Long commentId, Long userId) {

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 특정 게시물에 대한 특정 사용자의 좋아요를 찾아서 삭제
        commentLikeRepository.findByCommentAndUser(comment, user)
                .ifPresent(commentLikeRepository::delete);
    }
}