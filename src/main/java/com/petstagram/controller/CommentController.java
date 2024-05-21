package com.petstagram.controller;

import com.petstagram.dto.CommentDTO;
import com.petstagram.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/write/{postId}")
    public ResponseEntity<Long> writeComment(@PathVariable("postId") Long postId, @RequestBody CommentDTO commentDTO) {
        Long commentId = commentService.writeComment(postId, commentDTO);
        return new ResponseEntity<>(commentId, HttpStatus.OK);
    }

    // 댓글 리스트 조회
    @GetMapping("/list/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentList(@PathVariable("postId") Long postId) {
        List<CommentDTO> commentList = commentService.getCommentList(postId);
        return new ResponseEntity<>(commentList, HttpStatus.OK);
    }

    // 댓글 수정
    @PutMapping("/update/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable("commentId") Long commentId, @RequestBody CommentDTO commentDTO) {
        return ResponseEntity.ok(commentService.updateComment(commentId, commentDTO));
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable("commentId") Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("댓글 삭제에 실패헀습니다.");
        }
    }
}