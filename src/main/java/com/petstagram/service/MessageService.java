
package com.petstagram.service;

import com.petstagram.dto.MessageDTO;
import com.petstagram.entity.ChatRoomEntity;
import com.petstagram.entity.ImageEntity;
import com.petstagram.entity.MessageEntity;
import com.petstagram.entity.UserEntity;
import com.petstagram.repository.ChatRoomRepository;
import com.petstagram.repository.MessageRepository;
import com.petstagram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final FileUploadService fileUploadService;

    // 메시지 작성
    public MessageDTO sendMessage(MessageDTO messageDTO) {

        // 현재 인증된 사용자의 이름(또는 이메일 등) 가져오기
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 보내는 사람 찾기
        UserEntity sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 받는 사람 찾기
        UserEntity receiver = userRepository.findByEmail(messageDTO.getReceiverEmail())
                .orElseThrow(() -> new IllegalArgumentException("수신자가 존재하지 않습니다."));


        // DTO 를 Entity 로 변환하고 사용자 정보 설정
        MessageEntity messageEntity = MessageEntity.toEntity(messageDTO);
        messageEntity.setSender(sender);
        messageEntity.setReceiver(receiver);

        // 메시지 보내기와 받기 관련 연관 관계 설정
        sender.addSentMessage(messageEntity);
        receiver.addReceivedMessage(messageEntity);

        // 이미지 업로드 처리
//        handleFileUpload(file, messageEntity);

        // 메시지 저장
        MessageEntity save = messageRepository.save(messageEntity);

        return MessageDTO.toDTO(save);
    }

    // 두 사용자 간의 메시지 목록 조회
    @Transactional(readOnly = true)
    public List<MessageDTO> getMessageBetweenUsers(String receiverEmail) {
        // 현재 인증된 사용자의 이름(또는 이메일 등) 가져오기
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 메시지 목록 조회
        List<MessageEntity> messages = messageRepository.findMessagesBetweenUsers(senderEmail, receiverEmail);

        // Entity -> DTO 변환
        return messages.stream()
                .map(MessageDTO::toDTO)
                .collect(Collectors.toList());
    }

    // 이미지 업로드 처리 메서드
    private void handleFileUpload(MultipartFile file, MessageEntity messageEntity) {
        if (file != null && !file.isEmpty()) {
            String fileName = fileUploadService.storeFile(file);
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setImageUrl(fileName);
            imageEntity.setMessage(messageEntity);
            messageEntity.getImageList().add(imageEntity);
        }
    }
}