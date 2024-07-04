package com.petstagram.dto;

import com.petstagram.entity.ChatRoomEntity;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDTO {

    private Long id;
    private List<MessageDTO> messages; // 채팅방의 모든 메시지
    private Long senderId; // 발신자 ID
    private String senderName; // 발신자 이름
    private Long receiverId; // 수신자 ID
    private String receiverName; // 수신자 이름
    private Long unreadMessageCount;

    // Entity -> DTO 변환 메서드
    public static ChatRoomDTO toDTO(ChatRoomEntity chatRoom) {
        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .messages(chatRoom.getMessages().stream()
                        .map(MessageDTO::toDTO)
                        .collect(Collectors.toList()))
                .senderId(chatRoom.getSender().getId())
                .senderName(chatRoom.getSender().getName())
                .receiverId(chatRoom.getReceiver().getId())
                .receiverName(chatRoom.getReceiver().getName())
                .build();
    }
}