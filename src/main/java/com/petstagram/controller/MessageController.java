package com.petstagram.controller;

import com.petstagram.dto.MessageDTO;
import com.petstagram.entity.MessageEntity;
import com.petstagram.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user/message")
public class MessageController {

    private final MessageService messageService;

    // 메시지 보내기
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody MessageDTO messageDTO) {
//        try {
        return ResponseEntity.ok(messageService.sendMessage(messageDTO));
//        } catch (Exception e) {
//            log.error("파일 업로드 중 오류 발생", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메시지 작성에 실패했습니다.");
//        }
    }

    // 두 사용자 간의 메시지 목록 조회
    @GetMapping("/between/{receiverEmail}")
    public ResponseEntity<List<MessageDTO>> getChatRoomByUserId(@PathVariable String receiverEmail) {
        List<MessageDTO> messages = messageService.getMessageBetweenUsers(receiverEmail);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }
}