package com.petstagram.controller;

import com.petstagram.dto.PostDTO;
import com.petstagram.dto.UserDTO;
import com.petstagram.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping("/write")
    public ResponseEntity<String> writePost(@RequestPart("post") PostDTO postDTO, @RequestPart("file") MultipartFile file, @RequestPart("breed") String breed) {
        try {
            postDTO.setBreed(breed);
            postService.writePost(postDTO, file);
            return ResponseEntity.ok("게시글이 작성되었습니다.");
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 작성에 실패했습니다.");
        }
    }

    // 게시글 리스트 조회
    @GetMapping("/list")
    public ResponseEntity<List<PostDTO>> getPostList() {
        List<PostDTO> postList = postService.getPostList();
        return ResponseEntity.ok(postList);
    }

    // 사용자가 작성한 모든 게시물 조회
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PostDTO>> getPostsByUserId(@PathVariable("userId") Long userId) {
        List<PostDTO> postDTOList = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(postDTOList);
    }

    // 게시글 상세보기
    @GetMapping("/read/{postId}")
    public ResponseEntity<PostDTO> readPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.readPost(postId));
    }

    // 게시글 수정
    @PutMapping("/update/{postId}")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long postId,
                                              @RequestPart("post") PostDTO postDTO,
                                              @RequestPart(value = "file", required = false) MultipartFile file,
                                              @RequestPart(value = "breed", required = false) String breed,
                                              @RequestPart(value = "imageUrl", required = false) String imageUrl) {
        try {
            postDTO.setBreed(breed);
            PostDTO updatedPost = postService.updatePost(postId, postDTO, file, imageUrl);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            log.error("게시글 수정 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    // 게시글 삭제
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        try {
            postService.deletePost(postId);
            return ResponseEntity.ok("게시글이 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("게시글 삭제에 실패헀습니다.");
        }
    }

    // 게시물 좋아요 추가 및 삭제
    @PostMapping("/toggle/{postId}")
    public ResponseEntity<String> togglePostLike(@PathVariable("postId") Long postId) {
        postService.togglePostLike(postId);
        return ResponseEntity.ok("게시물에 좋아요가 추가되었습니다.");
    }

    // 게시물 좋아요 상태 조회
    @GetMapping("/status/{postId}")
    public ResponseEntity<PostDTO> getPostLikeStatus(@PathVariable("postId") Long postId) {
        PostDTO likeStatus = postService.getPostLikeStatus(postId);
        return ResponseEntity.ok(likeStatus);
    }

    // 특정 게시물에 좋아요를 누른 사용자 리스트 조회
    @GetMapping("/likes/{postId}")
    public ResponseEntity<List<UserDTO>> getPostLikesList(@PathVariable Long postId) {
        List<UserDTO> likedUsers = postService.getPostLikesList(postId);
        return ResponseEntity.ok(likedUsers);
    }
}