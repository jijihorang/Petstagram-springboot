package com.petstagram.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petstagram.dto.PostDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "posts")
public class PostEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id; // 게시물 고유 식별자

    private String postContent; // 게시물 내용(텍스트, 이미지, 비디오 링크 등).

    private String breed; // 강아지 종류 텐서플로우로 분류

    // 게시물과 사용자는 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user; // 게시물 작성자의 식별자.

    // 게시물과 이미지는 일대다 관계
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageEntity> imageList = new ArrayList<>();

    // 게시물과 댓글은 일대다 관계
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> commentList = new ArrayList<>();

    // 게시물과 좋아요 수는 일대다 관계
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PostLikeEntity> postLikeList = new HashSet<>();

    // DTO -> Entity
    public static PostEntity toEntity(PostDTO dto) {
        return PostEntity.builder()
                .postContent(dto.getPostContent())
                .breed(dto.getBreed())
                .imageList(new ArrayList<>())
                .commentList(new ArrayList<>())
                .build();
    }

    // 댓글을 포스트에 추가하는 메서드
    public void addComment(CommentEntity commentEntity) {
        this.commentList.add(commentEntity);
        commentEntity.setPost(this);
    }

    // 게시물 좋아요를 추가하는 메서드
    public void addLike(PostLikeEntity postLikeEntity) {
        this.postLikeList.add(postLikeEntity);
        postLikeEntity.setPost(this);
    }

    // 작성자 아이디를 가져오는 메서드
    public Long getAuthorId() {
        return this.user != null ? this.user.getId() : null;
    }
}