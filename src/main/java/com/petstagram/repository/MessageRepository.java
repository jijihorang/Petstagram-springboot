package com.petstagram.repository;

import com.petstagram.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query("SELECT m FROM MessageEntity m WHERE (m.sender.email = :senderEmail AND m.receiver.email = :receiverEmail) OR" +
            " (m.sender.email = :receiverEmail AND m.receiver.email = :senderEmail) ")
    List<MessageEntity> findMessagesBetweenUsers(@Param("senderEmail") String senderEmail, @Param("receiverEmail") String receiverEmail);

    List<MessageEntity> findByChatRoomId(Long roomId);

    // 채팅방 ID와 사용자 ID에 따른 읽지 않은 메시지 개수를 계산하는 쿼리 메서드
    @Query("SELECT COUNT(m) FROM MessageEntity m WHERE m.chatRoom.id = :chatRoomId AND m.isRead = false AND m.sender.id != :currentUserId")
    Long countUnreadMessages(@Param("chatRoomId") Long chatRoomId, @Param("currentUserId") Long currentUserId);

    // 사용자의 모든 읽지 않은 메시지 개수를 계산하는 쿼리 메서드
    @Query("SELECT COUNT(m) FROM MessageEntity m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessagesForUser(@Param("userId") Long userId);

}