package com.petstagram.controller;

import com.petstagram.dto.StoryDTO;
import com.petstagram.entity.StoryEntity;
import com.petstagram.entity.StoryReadEntity;
import com.petstagram.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/story")
@RequiredArgsConstructor
public class StoryController {

    private final StoryService storyService;

    /* 모든 회원의 유효한 스토리 조회 */
    @GetMapping("/list")
    public ResponseEntity<List<StoryDTO>> getAllStories() {
        List<StoryDTO> stories = storyService.getAllStories().stream()
                .map(StoryDTO::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stories);
    }

    /* 특정 사용자의 모든 스토리 조회 */
    @GetMapping("/all-list/{userId}")
    public ResponseEntity<List<StoryDTO>> getUserAllStories(@PathVariable Long userId) {
        List<StoryDTO> stories = storyService.getUserAllStories(userId).stream()
                .map(StoryDTO::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stories);
    }

    /* 특정 사용자의 유효한 스토리 조회 */
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<StoryDTO>> getUserStories(@PathVariable Long userId) {
        List<StoryDTO> stories = storyService.getUserStories(userId).stream()
                .map(StoryDTO::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stories);
    }

    /* 스토리 업로드 */
    @PostMapping("/upload")
    public ResponseEntity<String> createStory(@RequestPart("story") StoryDTO storyDTO,
                                              @RequestPart("file") List<MultipartFile> files) {
        try {
            System.out.println("Received story: " + storyDTO);
            System.out.println("Received files: " + files.size());
            storyService.createStory(storyDTO, files);
            return ResponseEntity.ok("스토리가 업로드 되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("스토리 업로드에 실패했습니다.");
        }
    }

    // 스토리 읽음 표시
    @PostMapping("/read/{storyId}")
    public ResponseEntity<Void> markStoryAsRead(@PathVariable Long storyId, @RequestParam Long userId) {
        storyService.markStoryAsRead(storyId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/read/{storyId}")
    public ResponseEntity<Boolean> isStoryRead(@PathVariable Long storyId, @RequestParam Long userId) {
        boolean isRead = storyService.isStoryRead(storyId, userId);
        return ResponseEntity.ok(isRead);
    }

    @GetMapping("/read/user/{userId}")
    public ResponseEntity<List<StoryReadEntity>> getUserReadStories(@PathVariable Long userId) {
        List<StoryReadEntity> readStories = storyService.getUserReadStories(userId);
        return ResponseEntity.ok(readStories);
    }
}