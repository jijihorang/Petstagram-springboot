package com.petstagram.dto;

import com.petstagram.entity.MessageEntity;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private Long id;
    private Long chatRoomId;
    private String messageContent;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private Long receiverId;
    private String receiverName;
    private String receiverEmail;
    private String regTime;
    private List<ImageDTO> imageList;
    private List<String> imageUrls;
    private List<VideoDTO> videoList;
    private List<String> videoUrls;
    private String audioUrl;

    // Entity -> DTO
    public static MessageDTO toDTO(MessageEntity messageEntity) {
        return MessageDTO.builder()
                .id(messageEntity.getId())
                .chatRoomId(messageEntity.getChatRoom().getId())
                .messageContent(messageEntity.getMessageContent())
                .senderId(messageEntity.getSender().getId())
                .senderName(messageEntity.getSender().getName())
                .senderEmail(messageEntity.getSender().getEmail())
                .receiverId(messageEntity.getReceiver().getId())
                .receiverName(messageEntity.getReceiver().getName())
                .receiverEmail(messageEntity.getReceiver().getEmail())
                .regTime(messageEntity.getRegTime().format(DateTimeFormatter.ISO_DATE_TIME))
                .imageList(messageEntity.getImageList().stream()
                        .map(ImageDTO::toDTO)
                        .collect(Collectors.toList()))
                .imageUrls(messageEntity.getImageList().stream()
                        .map(image -> image.getImageUrl())
                        .collect(Collectors.toList()))
                .videoList(messageEntity.getVideoList().stream()
                        .map(VideoDTO::toDTO)
                        .collect(Collectors.toList()))
                .videoUrls(messageEntity.getVideoList().stream()
                        .map(video -> video.getVideoUrl())
                        .collect(Collectors.toList()))
                .audioUrl(messageEntity.getAudioUrl()) // 음성 메시지 URL 설정
                .build();
    }
}
