package com.petstagram.service;

import com.petstagram.entity.StoryEntity;
import com.petstagram.repository.StoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryScheduler {
    private final StoryRepository storyRepository;

    @Scheduled(fixedRate = 3600000)
    public void deactivateOldStories() {
        LocalDateTime now = LocalDateTime.now();
        List<StoryEntity> stories = storyRepository.findAll();

        for (StoryEntity story : stories) {
            if (!story.isStoryExpired() && story.getRegTime().isBefore(now.minusHours(24))) {
                story.setStoryExpired(true);
            }
        }
        storyRepository.saveAll(stories);
    }
}

