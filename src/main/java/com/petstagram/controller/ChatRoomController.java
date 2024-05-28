package com.petstagram.controller;

import com.petstagram.dto.ChatRoomDTO;
import com.petstagram.entity.ChatRoomEntity;
import com.petstagram.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/chatRooms")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO) {
        ChatRoomDTO newChatRoom = chatRoomService.createChatRoom(chatRoomDTO);
        return ResponseEntity.ok(newChatRoom);
    }

    // 채팅방 및 메시지 목록 조회
    @PostMapping("/chatRooms/join/{roomId}")
    public ResponseEntity<ChatRoomDTO > addUserToChatRoom(@PathVariable Long roomId) {
        ChatRoomDTO updatedChatRoom = chatRoomService.addUserToChatRoom(roomId);
        return ResponseEntity.ok(updatedChatRoom);
    }
}