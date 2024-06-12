package com.petstagram.repository;

import com.petstagram.entity.ReplyCommentLikeEntity;
import com.petstagram.entity.ReplyCommentEntity;
import com.petstagram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReplyCommentLikeRepository extends JpaRepository<ReplyCommentLikeEntity, Long> {

    Optional<ReplyCommentLikeEntity> findByReplyCommentAndUser(ReplyCommentEntity replyComment, UserEntity user);

    long countByReplyComment(ReplyCommentEntity replyComment);

    List<ReplyCommentLikeEntity> findByReplyComment(ReplyCommentEntity replyComment);

    void deleteByReplyComment(ReplyCommentEntity replyComment);
}