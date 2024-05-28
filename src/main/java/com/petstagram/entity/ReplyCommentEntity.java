package com.petstagram.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

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
    private Long id; // 대댓글의 고유 식별자.

    @Column(nullable = false)
    private String replyCommentContent; // 대댓글 내용.

    // 대댓글과 사용자는 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY 는 지연 로딩을 의미
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user; // 대댓글 작성자의 식별자.

    // 대댓글과 댓글은 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY) // FetchType.LAZY 는 지연 로딩을 의미
    @JoinColumn(name = "comment_id")
    @JsonIgnore
    private CommentEntity comment; // 대댓글이 속한 댓글.

    // == 연관관계 편의 메서드 == //
    // 댓글에 대댓글 추가하는 메서드
    public void addReplyComment(ReplyCommentEntity replyCommentEntity) {
        replyCommentEntity.setComment(this.comment);
    }
}
