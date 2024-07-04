package com.petstagram.repository;

import com.petstagram.entity.StoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryRepository extends JpaRepository<StoryEntity, Long> {
    List<StoryEntity> findByUserId(Long userId);
    List<StoryEntity> findAllByStoryExpiredFalse();
    List<StoryEntity> findByUserIdAndStoryExpiredFalse(Long userId);
}