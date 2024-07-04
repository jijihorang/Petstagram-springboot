package com.petstagram.service;

import com.petstagram.dto.StoryDTO;
import com.petstagram.entity.*;
import com.petstagram.repository.StoryReadRepository;
import com.petstagram.repository.StoryRepository;
import com.petstagram.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class StoryService {

    private final StoryRepository storyRepository;
    private final StoryReadRepository storyReadRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    // 모든 회원 유효 스토리 조회
    public List<StoryEntity> getAllStories() {
        return storyRepository.findAllByStoryExpiredFalse();
    }

    // 특정 사용자의 모든 스토리 조회
    public List<StoryEntity> getUserAllStories(Long userId) {
        return storyRepository.findByUserId(userId);
    }

    // 특정 사용자의 유효한 스토리 조회
    public List<StoryEntity> getUserStories(Long userId) {
        return storyRepository.findByUserIdAndStoryExpiredFalse(userId);
    }

    // 스토리 저장
    @Transactional
    public void createStory(StoryDTO storyDTO, List<MultipartFile> files) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. email = " + username));

        StoryEntity storyEntity = StoryEntity.toEntity(storyDTO);
        userEntity.addStory(storyEntity);
        storyEntity.setUser(userEntity);
        storyEntity.setStoryExpired(false);
        storyEntity.setRegTime(LocalDateTime.now());

        // 파일 업로드 처리
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String fileName = fileUploadService.storeFile(file);
                    String contentType = file.getContentType();

                    if (contentType != null) {
                        if (contentType.startsWith("image/")) {
                            ImageEntity imageEntity = new ImageEntity();
                            imageEntity.setImageUrl(fileName);
                            imageEntity.setStory(storyEntity);
                            storyEntity.getImageList().add(imageEntity);
                        } else if (contentType.startsWith("video/")) {
                            VideoEntity videoEntity = new VideoEntity();
                            videoEntity.setVideoUrl(fileName);
                            videoEntity.setStory(storyEntity);
                            storyEntity.getVideoList().add(videoEntity);
                        }
                    }
                }
            }
        }

        // 스토리 저장
        storyRepository.save(storyEntity);
    }

    // 스토리 읽기
    @Transactional
    public void markStoryAsRead(Long storyId, Long userId) {
        StoryEntity story = storyRepository.findById(storyId).orElseThrow(() -> new RuntimeException("Story not found"));
        boolean alreadyRead = storyReadRepository.existsByStoryIdAndUserId(storyId, userId);

        if (story.getUser().getId().equals(userId)) {
            return;
        }

        if (!alreadyRead) {
            StoryReadEntity storyRead = new StoryReadEntity();
            storyRead.setUserId(userId);
            storyRead.setStory(story);
            storyReadRepository.save(storyRead);
        }
    }

    public boolean isStoryRead(Long storyId, Long userId) {
        return storyReadRepository.existsByStoryIdAndUserId(storyId, userId);
    }

    public List<StoryReadEntity> getUserReadStories(Long userId) {
        return storyReadRepository.findByUserId(userId);
    }
}
