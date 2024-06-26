package com.petstagram.repository;

import com.petstagram.entity.PostEntity;
import com.petstagram.entity.PostLikeEntity;
import com.petstagram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, Long> {

    Optional<PostLikeEntity> findByPostAndUser(PostEntity post, UserEntity user);

    // 특정 게시물에 대한 좋아요 개수 조회
    long countByPost(PostEntity post);

    // 특정 게시물에 좋아요르르 누른 사용자 리스트 조회
    List<PostLikeEntity> findByPost(PostEntity post);
}
