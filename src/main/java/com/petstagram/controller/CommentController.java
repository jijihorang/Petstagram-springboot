package com.petstagram.controller;

import com.petstagram.dto.CommentDTO;
import com.petstagram.dto.ReplyCommentDTO;
import com.petstagram.dto.UserDTO;
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

    // 댓글 좋아요 추가 및 삭제
    @PostMapping("/toggle/{commentId}")
    public ResponseEntity<String> toggleCommentLike(@PathVariable("commentId") Long commentId) {
        commentService.toggleCommentLike(commentId);
        return ResponseEntity.ok("게시물에 좋아요가 추가되었습니다.");
    }

    // 댓글 좋아요 상태 조회
    @GetMapping("/status/{commentId}")
    public ResponseEntity<CommentDTO> getPostCommentStatus(@PathVariable("commentId") Long commentId) {
        CommentDTO likeStatus = commentService.getCommentLikeStatus(commentId);
        return ResponseEntity.ok(likeStatus);
    }

    // 댓글 좋아요 사용자 조회
    @GetMapping("/liked/{commentId}")
    public ResponseEntity<List<UserDTO>> getUsers(@PathVariable("commentId") Long commentId) {
        List<UserDTO> liked = commentService.getCommentLikedUsers(commentId);
        return ResponseEntity.ok(liked);
    }

    // 대댓글 작성
    @PostMapping("/replywrite/{commentId}")
    public ResponseEntity<Long> writeReplyComment(@PathVariable("commentId") Long commentId, @RequestBody ReplyCommentDTO replyCommentDTO) {
        Long replyCommentId = commentService.writeReplyComment(commentId, replyCommentDTO);
        return new ResponseEntity<>(replyCommentId, HttpStatus.OK);
    }

    // 대댓글 리스트 조회
    @GetMapping("/replylist/{commentId}")
    public ResponseEntity<List<ReplyCommentDTO>> getReplyCommentList(@PathVariable("commentId") Long commentId) {
        List<ReplyCommentDTO> replyCommentList = commentService.getReplyCommentList(commentId);
        return new ResponseEntity<>(replyCommentList, HttpStatus.OK);
    }

    // 대댓글 삭제
    @DeleteMapping("/replydelete/{replyCommentId}")
    public ResponseEntity<String> deleteReplyComment(@PathVariable("replyCommentId") Long replyCommentId) {
        try {
            commentService.deleteReplyComment(replyCommentId);
            return ResponseEntity.ok("대댓글이 삭제되었습니다.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("대댓글 삭제 권한이 없습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("대댓글 삭제에 실패했습니다.");
        }
    }

    // 대댓글 좋아요 추가 또는 삭제
    @PostMapping("/replytoggle/{replyCommentId}")
    public ResponseEntity<String> toggleReplyCommentLike(@PathVariable("replyCommentId") Long replyCommentId) {
        commentService.toggleReplyCommentLike(replyCommentId);
        return ResponseEntity.ok("대댓글에 좋아요가 추가되었습니다.");
    }

    // 대댓글 좋아요 상태 조회
    @GetMapping("/replystatus/{replyCommentId}")
    public ResponseEntity<ReplyCommentDTO> getReplyCommentLikeStatus(@PathVariable("replyCommentId") Long replyCommentId) {
        ReplyCommentDTO likeStatus = commentService.getReplyCommentLikeStatus(replyCommentId);
        return ResponseEntity.ok(likeStatus);
    }

    // 대댓글 좋아요를 누른 사용자 리스트 조회
    @GetMapping("/replyliked/{replyCommentId}")
    public ResponseEntity<List<UserDTO>> getReplyCommentLikedUsers(@PathVariable("replyCommentId") Long replyCommentId) {
        List<UserDTO> liked = commentService.getReplyCommentLikedUsers(replyCommentId);
        return ResponseEntity.ok(liked);
    }
}