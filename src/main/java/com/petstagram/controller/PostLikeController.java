package com.petstagram.controller;

import com.petstagram.dto.PostLikeDTO;
import com.petstagram.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/like")
public class PostLikeController {

    private final PostLikeService postLikeService;

    // 게시물 좋아요 추가 및 삭제
    @PostMapping("/post/toggle/{postId}")
    public ResponseEntity<String> togglePostLike(@PathVariable("postId") Long postId) {
        postLikeService.togglePostLike(postId);
        return ResponseEntity.ok("게시물에 좋아요가 추가되었습니다.");
    }

    // 게시물 좋아요 상태 조회
    @GetMapping("/post/status/{postId}")
    public ResponseEntity<PostLikeDTO> getPostLikeStatus(@PathVariable("postId") Long postId) {
        PostLikeDTO likeStatus = postLikeService.getPostLikeStatus(postId);
        return ResponseEntity.ok(likeStatus);
    }
}