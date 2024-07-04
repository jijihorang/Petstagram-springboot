package com.petstagram.service;

import com.petstagram.dto.ChatRoomDTO;
import com.petstagram.dto.MessageDTO;
import com.petstagram.entity.*;
import com.petstagram.repository.ChatRoomRepository;
import com.petstagram.repository.MessageRepository;
import com.petstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<String, Long> activeUserRooms = new ConcurrentHashMap<>();
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public void setUserActiveRoom(String email, Long roomId) {
        activeUserRooms.put(email, roomId);
    }

    public void removeUserActiveRoom(String email) {
        activeUserRooms.remove(email);
    }

    public Long getUserActiveRoom(String email) {
        return activeUserRooms.get(email);
    }

    // 채팅방 생성
    @Transactional
    public ChatRoomDTO createChatRoom(ChatRoomDTO chatRoomDTO) {
        Optional<ChatRoomEntity> existingChatRoom = chatRoomRepository.findBySenderIdAndReceiverId(
                chatRoomDTO.getSenderId(), chatRoomDTO.getReceiverId()
        );

        if (existingChatRoom.isPresent()) {
            return ChatRoomDTO.toDTO(existingChatRoom.get());
        }

        // 새로운 채팅방 생성
        ChatRoomEntity chatRoom = new ChatRoomEntity();
        chatRoom.setMessages(new ArrayList<>());
        chatRoom.setSender(userRepository.findById(chatRoomDTO.getSenderId()).orElseThrow());
        chatRoom.setReceiver(userRepository.findById(chatRoomDTO.getReceiverId()).orElseThrow());

        chatRoom.getSender().addSentChatRoom(chatRoom);
        chatRoom.getReceiver().addReceivedChatRoom(chatRoom);

        // 채팅방 저장
        ChatRoomEntity savedChatRoom = chatRoomRepository.save(chatRoom);

        return ChatRoomDTO.toDTO(savedChatRoom);
    }

    // 메시지 작성
    @Transactional
    public MessageDTO sendMessage(MessageDTO messageDTO, Principal principal) {

        String name = principal.getName();
        UserEntity sender = userRepository.findByEmail(name)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 받는 사람 찾기
        UserEntity receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("수신자가 존재하지 않습니다."));

        // 채팅방 찾기
        ChatRoomEntity chatRoom = chatRoomRepository.findById(messageDTO.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // DTO 를 Entity 로 변환하고 사용자 정보 설정
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setMessageContent(messageDTO.getMessageContent());
        messageEntity.setImageList(new ArrayList<>());
        messageEntity.setChatRoom(chatRoom);
        messageEntity.setSender(sender);
        messageEntity.setReceiver(receiver);
        messageEntity.setRegTime(LocalDateTime.now());

        // 이미지 URL 을 저장
        if (messageDTO.getImageUrls() != null && !messageDTO.getImageUrls().isEmpty()) {
            for (String imageUrl : messageDTO.getImageUrls()) {
                try {
                    ImageEntity imageEntity = new ImageEntity();
                    imageEntity.setImageUrl(imageUrl);
                    imageEntity.setMessage(messageEntity);

                    messageEntity.getImageList().add(imageEntity);
                } catch (Exception e) {
                    throw new RuntimeException("이미지 저장에 실패했습니다.", e);
                }
            }
        }

        // 동영상 URL 을 저장
        if (messageDTO.getVideoUrls() != null && !messageDTO.getVideoUrls().isEmpty()) {
            for (String videoUrl : messageDTO.getVideoUrls()) {
                try {
                    VideoEntity videoEntity = new VideoEntity();
                    videoEntity.setVideoUrl(videoUrl);
                    videoEntity.setMessage(messageEntity);

                    messageEntity.getVideoList().add(videoEntity);
                } catch (Exception e) {
                    throw new RuntimeException("동영상 저장에 실패했습니다.", e);
                }
            }
        }

        // 연관관계 편의 메서드 설정
        chatRoom.addMessage(messageEntity);

        // 메시지 저장
        MessageEntity savedMessage = messageRepository.save(messageEntity);

        return MessageDTO.toDTO(savedMessage);
    }

    @Transactional
    public MessageDTO sendAudioMessage(MessageDTO messageDTO, Principal principal) {

        String name = principal.getName();
        UserEntity sender = userRepository.findByEmail(name)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 받는 사람 찾기
        UserEntity receiver = userRepository.findById(messageDTO.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("수신자가 존재하지 않습니다."));

        // 채팅방 찾기
        ChatRoomEntity chatRoom = chatRoomRepository.findById(messageDTO.getChatRoomId())
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // DTO 를 Entity 로 변환하고 사용자 정보 설정
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setAudioUrl(messageDTO.getAudioUrl());  // 음성 메시지 URL 설정
        messageEntity.setChatRoom(chatRoom);
        messageEntity.setSender(sender);
        messageEntity.setReceiver(receiver);
        messageEntity.setRegTime(LocalDateTime.now());

        // 연관관계 편의 메서드 설정
        chatRoom.addMessage(messageEntity);

        // 메시지 저장
        MessageEntity savedMessage = messageRepository.save(messageEntity);

        return MessageDTO.toDTO(savedMessage);
    }

    // 채팅방 리스트 조회
    public List<ChatRoomDTO> getChatRoomList(Principal principal) {

        String name = principal.getName();
        UserEntity currentUser = userRepository.findByEmail(name)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 사용자가 송신자 또는 수신자인 모든 채팅방 목록을 가져옴
        List<ChatRoomEntity> allChatRooms = Stream.concat(
                        chatRoomRepository.findBySender(currentUser).stream(),
                        chatRoomRepository.findByReceiver(currentUser).stream())
                .toList();

        // 각 채팅방을 ChatRoomDTO 로 변환하여 리스트에 추가하고 읽지 않은 메시지 여부 확인
        List<ChatRoomDTO> chatRoomDTOs = allChatRooms.stream()
                .map(chatRoomEntity -> {
                    List<MessageEntity> recentMessages = chatRoomRepository.findRecentMessagesByChatRoomId(chatRoomEntity.getId());

                    // 현재 활성 채팅방 ID를 가져옴
                    Long activeRoomId = getUserActiveRoom(name);

                    // 읽지 않은 메시지 개수 계산
                    Long unreadMessageCount;
                    if (activeRoomId != null && activeRoomId.equals(chatRoomEntity.getId())) {
                        unreadMessageCount = 0L; // 활성 채팅방인 경우 읽지 않은 메시지 개수는 0
                    } else {
                        unreadMessageCount = messageRepository.countUnreadMessages(chatRoomEntity.getId(), currentUser.getId());
                    }

                    return ChatRoomDTO.builder()
                            .id(chatRoomEntity.getId())
                            .messages(recentMessages.stream().map(MessageDTO::toDTO).collect(Collectors.toList()))
                            .senderId(chatRoomEntity.getSender().getId())
                            .senderName(chatRoomEntity.getSender().getName())
                            .receiverId(chatRoomEntity.getReceiver().getId())
                            .receiverName(chatRoomEntity.getReceiver().getName())
                            .unreadMessageCount(unreadMessageCount)
                            .build();
                })
                .sorted(Comparator.comparing(chatRoom -> {
                    if (chatRoom.getMessages().isEmpty()) {
                        return LocalDateTime.MIN;
                    } else {
                        String regTime = chatRoom.getMessages().get(0).getRegTime();
                        return LocalDateTime.parse(regTime, DateTimeFormatter.ISO_DATE_TIME);
                    }
                }, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return chatRoomDTOs;
    }

    // 송신자와 수신자가 서로 다른 사용자와 대화한 채팅방만 목록에 포함
    public List<ChatRoomDTO> getActiveChatRoomList(Principal principal) {
        String name = principal.getName();
        UserEntity currentUser = userRepository.findByEmail(name)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 현재 사용자가 송신자 또는 수신자인 모든 채팅방과 관련된 메시지를 패치 조인을 통해 한 번에 로드
        List<ChatRoomEntity> activeChatRooms = chatRoomRepository.findActiveChatRoomsByUser(currentUser);

        // 각 채팅방의 ID를 추출하여 중복을 제거하고 필터링
        Set<Long> distinctChatRoomIds = activeChatRooms.stream()
                .map(ChatRoomEntity::getId)
                .collect(Collectors.toSet());

        // 활성 채팅방 ID 목록을 기반으로 채팅방 엔티티 조회
        List<ChatRoomEntity> distinctActiveChatRooms = chatRoomRepository.findAllById(new ArrayList<>(distinctChatRoomIds));

        // 각 채팅방을 ChatRoomDTO 로 변환하여 리스트에 추가하고, 메시지의 도착 시간에 따라 정렬
        List<ChatRoomDTO> chatRoomDTOs = distinctActiveChatRooms.stream()
                .map(chatRoomEntity -> {
                    List<MessageEntity> recentMessages = chatRoomRepository.findRecentMessagesByChatRoomId(chatRoomEntity.getId());

                    // 데이터베이스에서 읽지 않은 메시지 개수 계산
                    Long unreadMessageCount = messageRepository.countUnreadMessages(chatRoomEntity.getId(), currentUser.getId());

                    return ChatRoomDTO.builder()
                            .id(chatRoomEntity.getId())
                            .messages(recentMessages.stream().map(MessageDTO::toDTO).collect(Collectors.toList()))
                            .senderId(chatRoomEntity.getSender().getId())
                            .senderName(chatRoomEntity.getSender().getName())
                            .receiverId(chatRoomEntity.getReceiver().getId())
                            .receiverName(chatRoomEntity.getReceiver().getName())
                            .unreadMessageCount(unreadMessageCount)
                            .build();
                })
                .sorted(Comparator.comparing(chatRoom -> {
                    if (chatRoom.getMessages().isEmpty()) {
                        return LocalDateTime.MIN;
                    } else {
                        String regTime = chatRoom.getMessages().get(0).getRegTime();
                        return LocalDateTime.parse(regTime, DateTimeFormatter.ISO_DATE_TIME);
                    }
                }, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return chatRoomDTOs;
    }

    // 채팅방 ID에 해당하는 채팅방과 메시지들을 가져오고 읽음으로 처리하는 메서드
    @Transactional
    public ChatRoomDTO getChatRoomWithMessagesByIdAndMarkAsRead(Long chatRoomId, Principal principal) {

        // 현재 사용자를 가져오기
        UserEntity currentUser = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 채팅방 찾기
        ChatRoomEntity chatRoom = chatRoomRepository.findChatRoomWithMessagesById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("채팅방을 찾을 수 없습니다."));

        // 채팅방에 속한 가장 최근 메시지 목록 조회 (내림차순으로 정렬)
        List<MessageEntity> messages = chatRoomRepository.findRecentMessagesByChatRoomId(chatRoomId);

        // 새로운 메시지가 있는지 여부를 추적
        boolean hasNewMessages = false;

        // 현재 사용자가 채팅방에 속해 있다면
        for (MessageEntity message : messages) {
            if (!message.isRead() && !message.getSender().getId().equals(currentUser.getId())) {
                message.setRead(true);
                messageRepository.save(message); // 메시지 상태 갱신
                hasNewMessages = true;
            }
        }

        // 사용자의 모든 읽지 않은 메시지 개수를 계산
        Long unreadMessageCount = messageRepository.countUnreadMessagesForUser(currentUser.getId());

        // ChatRoomDTO 변환
        ChatRoomDTO chatRoomDTO = ChatRoomDTO.toDTO(chatRoom);

        // 메시지 정보 설정
        List<MessageDTO> messageDTOs = messages.stream()
                .map(MessageDTO::toDTO)
                .collect(Collectors.toList());

        chatRoomDTO.setMessages(messageDTOs);
        chatRoomDTO.setUnreadMessageCount(unreadMessageCount);

        // 새로운 메시지가 있을 경우에만 메시지 전송
        if (hasNewMessages) {
            // 체팅방 리스트 조회
            List<ChatRoomDTO> updatedChatRoomList = getActiveChatRoomList(principal);

            messagingTemplate.convertAndSend("/sub/chatRoomList/" + currentUser.getEmail(), unreadMessageCount);
            messagingTemplate.convertAndSend("/sub/chatRoomList/" + currentUser.getEmail(), updatedChatRoomList);
        }


        return chatRoomDTO;
    }
}