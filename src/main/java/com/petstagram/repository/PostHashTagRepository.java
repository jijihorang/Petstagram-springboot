package com.petstagram.repository;

import com.petstagram.entity.PostHashTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHashTagRepository extends JpaRepository<PostHashTagEntity, Long> {
    void deleteByPostId(Long postId);
}
