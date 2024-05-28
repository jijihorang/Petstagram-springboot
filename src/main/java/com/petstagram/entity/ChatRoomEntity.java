package com.petstagram.entity;

import com.petstagram.dto.ChatRoomDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "chatRooms")
public class ChatRoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatRoom_id")
    private Long id;

    // 채팅룸과 메시지 는 일대다 관계
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messages = new ArrayList<>();

    // 채팅룸과 사용자는 다대다 관계
    @ManyToMany
    @JoinTable(
            name = "user_chatroom", // 이름 일치시키기
            joinColumns = @JoinColumn(name = "chatRoom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> users = new HashSet<>();

    // DTO -> Entity 변환 메서드
    public static ChatRoomEntity toEntity(ChatRoomDTO chatRoomDTO, Set<UserEntity> userEntities) {
        return ChatRoomEntity.builder()
                .id(chatRoomDTO.getId())
                .messages(new ArrayList<>())
                .users(userEntities)
                .build();
    }

    // 단일 메시지를 추가하는 기존 메서드 유지
    public void addMessage(MessageEntity message) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(message);
        message.setChatRoom(this);
    }

    // 여러 메시지를 한 번에 추가하는 메서드
    public void addMessages(List<MessageEntity> messages) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        messages.forEach(this::addMessage); // Java 8의 메서드 참조 사용
    }
}