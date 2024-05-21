package com.petstagram.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostLikeDTO {
    private boolean liked; // 게시물 좋아요 상태
    private long likesCount; // 게시물의 좋아요 수.
}