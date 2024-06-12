package com.petstagram.repository;

import com.petstagram.entity.ReplyCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyCommentRepository extends JpaRepository<ReplyCommentEntity, Long> {
    List<ReplyCommentEntity> findByCommentId(Long commentId);
}