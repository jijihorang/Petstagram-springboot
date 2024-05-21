package com.petstagram.repository;

import com.petstagram.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findAllByOrderByIdDesc();
    List<PostEntity> findByUserId(Long userId);
}
