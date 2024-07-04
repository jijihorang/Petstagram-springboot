package com.petstagram.repository;

import com.petstagram.entity.StoryReadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryReadRepository extends JpaRepository<StoryReadEntity, Long> {
    boolean existsByStoryIdAndUserId(Long storyId, Long userId);
    List<StoryReadEntity> findByUserId(Long userId);
}