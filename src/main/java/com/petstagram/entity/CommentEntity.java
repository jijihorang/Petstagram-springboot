
package com.petstagram.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petstagram.dto.CommentDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 댓글
@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @Column(nullable = false)
    private String commentContent;

    // 댓글과 사용자는 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;

    // 댓글과 게시물은 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private PostEntity post;

    // 댓글과 좋아요 수는 일대다 관계
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CommentLikeEntity> commentLikeList = new HashSet<>();

    // 대댓글과의 관계 설정
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReplyCommentEntity> replyCommentList = new ArrayList<>();

    // DTO -> Entity
    public static CommentEntity toEntity(CommentDTO dto) {
        return CommentEntity.builder()
                .commentContent(dto.getCommentContent())
                .build();
    }

    // == 연관관계 편의 메서드 == //
    // 댓글 좋아요를 추가하는 메서드
    public void addCommentLike(CommentLikeEntity commentLikeEntity) {
        this.commentLikeList.add(commentLikeEntity);
        commentLikeEntity.setComment(this); // 댓글에 좋아요 설정
    }

    // 대댓글을 추가하는 메서드
    public void addReplyComment(ReplyCommentEntity replyCommentEntity) {
        this.replyCommentList.add(replyCommentEntity);
        replyCommentEntity.setComment(this);
    }

    // 댓글 작성자의 ID를 반환하는 메서드
    public Long getAuthorId() {
        return this.user != null ? this.user.getId() : null;
    }
}
