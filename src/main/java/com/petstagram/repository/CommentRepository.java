package com.petstagram.repository;

import com.petstagram.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByPostId(Long postId);

    long countByPostId(long postId);

    @Query("SELECT c FROM CommentEntity c JOIN FETCH c.user WHERE c.post.id = :postId")
    List<CommentEntity> findByPostIdWithUser(@Param("postId") Long postId);

    @Query("SELECT c FROM CommentEntity c JOIN FETCH c.user WHERE c.id = :commentId")
    Optional<CommentEntity> findByIdAndFetchUser(@Param("commentId") Long commentId);
}