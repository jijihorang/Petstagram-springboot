package com.petstagram.dto;

import com.petstagram.entity.ChatRoomEntity;
import com.petstagram.entity.CommentEntity;
import com.petstagram.entity.MessageEntity;
import com.petstagram.entity.UserEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
public class ChatRoomDTO {

    private Long id;
    private List<MessageDTO> messages;
    private Set<String> userEmails; // 사용자 이메일 목록

    // Entity -> DTO
    public static ChatRoomDTO toDTO(ChatRoomEntity chatRoom) {
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .messages(chatRoom.getMessages().stream()
                        .map(MessageDTO::toDTO)
                        .collect(Collectors.toList()))
                .userEmails(chatRoom.getUsers().stream()
                        .map(UserEntity::getEmail)
                        .collect(Collectors.toSet()))
                .build();
    }
}