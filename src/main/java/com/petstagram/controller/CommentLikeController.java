package com.petstagram.controller;

import com.petstagram.entity.UserEntity;
import com.petstagram.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    // 댓글 좋아요 추가
    @PostMapping("/comment/add/{commentId}")
    public ResponseEntity<String> addPostLike(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal UserEntity user) {
        commentLikeService.addCommentLike(commentId, user.getId());
        return ResponseEntity.ok("댓글에 좋아요가 추가되었습니다.");
    }

    // 댓글 좋아요 삭제
    @DeleteMapping("/comment/remove/{commentId}")
    public ResponseEntity<String> removeCommentLike(@PathVariable("commentId") Long commentId, @AuthenticationPrincipal UserEntity user) {
        commentLikeService.removeCommentLike(commentId, user.getId());
        return ResponseEntity.ok("댓글에 좋아요가 취소되었습니다.");
    }
}