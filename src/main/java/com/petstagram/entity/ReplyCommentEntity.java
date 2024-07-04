package com.petstagram.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reply_comments")
public class ReplyCommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_comment_id")
    private Long id;

    @Column(nullable = false)
    private String replyCommentContent;

    // 대댓글과 사용자는 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    // 대댓글과 댓글은 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    @JsonIgnore
    private CommentEntity comment;

    // 대댓글과 좋아요 수는 일대다 관계
    @OneToMany(mappedBy = "replyComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReplyCommentLikeEntity> replyCommentLikeList = new HashSet<>();

    public Long getPostId() {
        return comment.getPost().getId();
    }

    // == 연관관계 편의 메서드 == //
    // 댓글에 대댓글 추가하는 메서드
    public void addReplyComment(ReplyCommentEntity replyCommentEntity) {
        replyCommentEntity.setComment(this.comment);
    }
}