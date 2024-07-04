package com.petstagram.controller;

import com.petstagram.dto.ChatRoomDTO;
import com.petstagram.dto.MessageDTO;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.UserRepository;
import com.petstagram.service.ChatRoomService;
import com.petstagram.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;

    // 채팅방 생성
    @PostMapping("/chatRooms")
    public ResponseEntity<ChatRoomDTO> createChatRoom(@RequestBody ChatRoomDTO chatRoomDTO, Principal principal) {
        ChatRoomDTO newChatRoom = chatRoomService.createChatRoom(chatRoomDTO);

        chatRoomService.setUserActiveRoom(principal.getName(), newChatRoom.getId());

        return ResponseEntity.ok(newChatRoom);
    }

    // 채팅방 리스트 조회
    @GetMapping("/chatRooms/list")
    public ResponseEntity<List<ChatRoomDTO>> getChatRoomList(Principal principal) {
        List<ChatRoomDTO> chatRoomList = chatRoomService.getChatRoomList(principal);
        return ResponseEntity.ok(chatRoomList);
    }

    // 메시지 전송
    @MessageMapping("/sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @RequestBody MessageDTO messageDTO, Principal principal) {
        MessageDTO sentMessage = chatRoomService.sendMessage(messageDTO, principal);

        // 메시지 전송 후, 해당 채팅방을 구독하는 클라이언트에게 메시지 정보 업데이트 알림
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, sentMessage);

        String senderEmail = principal.getName();

        Long receiverId = sentMessage.getReceiverId();
        UserEntity receiverUser = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("수신자를 찾을 수 없습니다."));
        String receiverEmail = receiverUser.getEmail();

        Comparator<ChatRoomDTO> sortByRegTimeDesc = Comparator.comparing(chatRoom -> {
            if (chatRoom.getMessages().isEmpty()) {
                return LocalDateTime.MIN;
            } else {
                String regTime = chatRoom.getMessages().get(0).getRegTime();
                return LocalDateTime.parse(regTime, DateTimeFormatter.ISO_DATE_TIME);
            }
        }, Comparator.reverseOrder());

        // 발신자와 수신자의 채팅방 리스트 업데이트
        List<ChatRoomDTO> updatedChatRoomListSender = chatRoomService.getActiveChatRoomList(principal)
                .stream()
                .sorted(sortByRegTimeDesc)
                .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/sub/chatRoomList/" + senderEmail, updatedChatRoomListSender);

        List<ChatRoomDTO> updatedChatRoomListReceiver = chatRoomService.getActiveChatRoomList(() -> receiverEmail)
                .stream()
                .sorted(sortByRegTimeDesc)
                .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/sub/chatRoomList/" + receiverEmail, updatedChatRoomListReceiver);

        // 발신자와 수신자가 해당 채팅방에 있는지 확인
        Long senderActiveRoomId = chatRoomService.getUserActiveRoom(senderEmail);
        Long receiverActiveRoomId = chatRoomService.getUserActiveRoom(receiverEmail);

        // 발신자가 채팅방에 있는 경우, 메시지를 읽음 처리
        if (senderActiveRoomId != null && senderActiveRoomId.equals(roomId)) {
            chatRoomService.getChatRoomWithMessagesByIdAndMarkAsRead(roomId, principal);
        } else {
            Long senderUnreadCount = updatedChatRoomListSender.stream()
                    .mapToLong(ChatRoomDTO::getUnreadMessageCount)
                    .sum();
            messagingTemplate.convertAndSend("/sub/messageCount/" + senderEmail, senderUnreadCount);
        }

        // 수신자가 채팅방에 있는 경우, 메시지를 읽음 처리
        if (receiverActiveRoomId != null && receiverActiveRoomId.equals(roomId)) {
            chatRoomService.getChatRoomWithMessagesByIdAndMarkAsRead(roomId, () -> receiverEmail);
        } else {
            Long receiverUnreadCount = updatedChatRoomListReceiver.stream()
                    .mapToLong(ChatRoomDTO::getUnreadMessageCount)
                    .sum();
            messagingTemplate.convertAndSend("/sub/messageCount/" + receiverEmail, receiverUnreadCount);
        }
    }

    // 음성 메시지 전송
    @MessageMapping("/sendAudioMessage/{roomId}")
    public void sendAudioMessage(@DestinationVariable Long roomId, @RequestBody MessageDTO messageDTO, Principal principal) {
        MessageDTO sentMessage = chatRoomService.sendAudioMessage(messageDTO, principal);

        // 메시지 전송 후, 해당 채팅방을 구독하는 클라이언트에게 메시지 정보 업데이트 알림
        messagingTemplate.convertAndSend("/sub/chat/room/" + roomId, sentMessage);

        String senderEmail = principal.getName();

        Long receiverId = sentMessage.getReceiverId();
        UserEntity receiverUser = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("수신자를 찾을 수 없습니다."));
        String receiverEmail = receiverUser.getEmail();

        Comparator<ChatRoomDTO> sortByRegTimeDesc = Comparator.comparing(chatRoom -> {
            if (chatRoom.getMessages().isEmpty()) {
                return LocalDateTime.MIN;
            } else {
                String regTime = chatRoom.getMessages().get(0).getRegTime();
                return LocalDateTime.parse(regTime, DateTimeFormatter.ISO_DATE_TIME);
            }
        }, Comparator.reverseOrder());

        // 발신자와 수신자의 채팅방 리스트 업데이트
        List<ChatRoomDTO> updatedChatRoomListSender = chatRoomService.getActiveChatRoomList(principal)
                .stream()
                .sorted(sortByRegTimeDesc)
                .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/sub/chatRoomList/" + senderEmail, updatedChatRoomListSender);

        List<ChatRoomDTO> updatedChatRoomListReceiver = chatRoomService.getActiveChatRoomList(() -> receiverEmail)
                .stream()
                .sorted(sortByRegTimeDesc)
                .collect(Collectors.toList());
        messagingTemplate.convertAndSend("/sub/chatRoomList/" + receiverEmail, updatedChatRoomListReceiver);

        // 발신자와 수신자가 해당 채팅방에 있는지 확인
        Long senderActiveRoomId = chatRoomService.getUserActiveRoom(senderEmail);
        Long receiverActiveRoomId = chatRoomService.getUserActiveRoom(receiverEmail);

        // 발신자가 채팅방에 있는 경우, 메시지를 읽음 처리
        if (senderActiveRoomId != null && senderActiveRoomId.equals(roomId)) {
            chatRoomService.getChatRoomWithMessagesByIdAndMarkAsRead(roomId, principal);
        } else {
            Long senderUnreadCount = updatedChatRoomListSender.stream()
                    .mapToLong(ChatRoomDTO::getUnreadMessageCount)
                    .sum();
            messagingTemplate.convertAndSend("/sub/messageCount/" + senderEmail, senderUnreadCount);
        }

        // 수신자가 채팅방에 있는 경우, 메시지를 읽음 처리
        if (receiverActiveRoomId != null && receiverActiveRoomId.equals(roomId)) {
            chatRoomService.getChatRoomWithMessagesByIdAndMarkAsRead(roomId, () -> receiverEmail);
        } else {
            Long receiverUnreadCount = updatedChatRoomListReceiver.stream()
                    .mapToLong(ChatRoomDTO::getUnreadMessageCount)
                    .sum();
            messagingTemplate.convertAndSend("/sub/messageCount/" + receiverEmail, receiverUnreadCount);
        }
    }

    // 채팅방 입장 및 메시지 목록 조회, 메시지 개수 초기화
    @GetMapping("/chatRooms/{chatRoomId}")
    public ResponseEntity<ChatRoomDTO> getChatRoomWithMessagesById(@PathVariable Long chatRoomId, Principal principal) {
        // 채팅방 및 메시지 조회
        ChatRoomDTO chatRoomDTO = chatRoomService.getChatRoomWithMessagesByIdAndMarkAsRead(chatRoomId, principal);

        // 사용자가 해당 채팅방에 들어가 있음을 기록
        chatRoomService.setUserActiveRoom(principal.getName(), chatRoomId);

        // 읽지 않은 메시지 개수 계산 후 WebSocket 으로 전송
        String receiverEmail = principal.getName();
        Long receiverUnreadCount = chatRoomDTO.getUnreadMessageCount();
        messagingTemplate.convertAndSend("/sub/messageCount/" + receiverEmail, receiverUnreadCount);

        return ResponseEntity.ok(chatRoomDTO);
    }

    // 채팅방 퇴장
    @MessageMapping("/leaveRoom/{roomId}")
    public void leaveRoom(@DestinationVariable Long roomId, Principal principal) {
        chatRoomService.removeUserActiveRoom(principal.getName());
    }

    // 사용자가 참여한 모든 채팅방에서의 읽지 않은 메시지 개수를 합산하여 반환
    @GetMapping("/unreadMessageCount")
    public ResponseEntity<Long> getUnreadMessageCount(Principal principal) {

        List<ChatRoomDTO> chatRoomList = chatRoomService.getActiveChatRoomList(principal);

        // 읽지 않은 메시지 개수를 합산
        Long totalUnreadCount = chatRoomList.stream()
                .mapToLong(ChatRoomDTO::getUnreadMessageCount)
                .sum();

        return ResponseEntity.ok(totalUnreadCount);
    }

    // 이미지 파일 업로드 후 URL 반환
    @PostMapping("/uploadImage")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 파일 업로드 처리 로직
            String imageUrl = fileUploadService.storeFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 업로드 실패 시 에러 메시지 반환
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // 음성 파일 업로드 후 URL 반환
    @PostMapping("/uploadAudio")
    public ResponseEntity<Map<String, String>> uploadAudio(@RequestParam("file") MultipartFile file) {
        try {
            // 파일 업로드 처리 로직
            String audioUrl = fileUploadService.storeFile(file);

            Map<String, String> response = new HashMap<>();
            response.put("audioUrl", audioUrl);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 업로드 실패 시 에러 메시지 반환
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload audio: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/uploads/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        try {
            Path filePath = fileUploadService.loadFileAsResource(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}