package com.petstagram.dto;

import com.petstagram.entity.FollowEntity;
import com.petstagram.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowDTO {

    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private Boolean status;

    // FollowEntity에서 FollowDTO로 변환하는 메소드
    public static FollowDTO fromEntity(FollowEntity followEntity) {
        return FollowDTO.builder()
                .id(followEntity.getId())
                .fromUserId(followEntity.getFromUser().getId())
                .toUserId(followEntity.getToUser().getId())
                .status(followEntity.getStatus())
                .build();
    }

    // FollowDTO에서 FollowEntity로 변환하는 메소드
    public static FollowEntity toEntity(FollowDTO followDTO, UserEntity fromUser, UserEntity toUser) {
        return FollowEntity.builder()
                .id(followDTO.getId())
                .fromUser(fromUser)
                .toUser(toUser)
                .status(followDTO.getStatus())
                .build();
    }
}
