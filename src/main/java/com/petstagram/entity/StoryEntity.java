package com.petstagram.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petstagram.dto.StoryDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stories")
public class StoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long id;

    private String storyText;

    private String storyType;

    private boolean storyExpired;

    // 스토리와 사용자는 다대일 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user; // 스토리 작성자의 식별자.

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoryReadEntity> reads = new HashSet<>();

    // 스토리와 이미지는 일대다 관계
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageEntity> imageList = new ArrayList<>();

    // 스토리와 동영상은 일대다 관계
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VideoEntity> videoList = new ArrayList<>();

    // DTO -> Entity
    public static StoryEntity toEntity(StoryDTO dto) {
        return StoryEntity.builder()
                .storyText(dto.getStoryText())
                .storyType(dto.getStoryType())
                .imageList(new ArrayList<>())
                .videoList(new ArrayList<>())
                .storyExpired(false)
                .build();
    }

    // 연관관계 편의 메서드
    public void addRead(StoryReadEntity storyRead) {
        reads.add(storyRead);
        storyRead.setStory(this);
    }

}