package com.petstagram.repository;

import com.petstagram.entity.ChatRoomEntity;
import com.petstagram.entity.MessageEntity;
import com.petstagram.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoomEntity, Long> {

    // 특정 채팅 방의 가장 최근 메시지를 가져오는 쿼리
    @Query("SELECT message FROM MessageEntity message WHERE message.chatRoom.id = :chatRoomId ORDER BY message.regTime DESC")
    List<MessageEntity> findRecentMessagesByChatRoomId(@Param("chatRoomId") Long chatRoomId);

    List<ChatRoomEntity> findBySender(UserEntity currentUser);

    List<ChatRoomEntity> findByReceiver(UserEntity currentUser);

    Optional<ChatRoomEntity> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    // 현재 사용자가 송신자 또는 수신자인 모든 채팅방과 관련된 메시지를 패치 조인을 통해 한 번에 로드
    @Query("SELECT DISTINCT cr " +
            "FROM ChatRoomEntity cr " +
            "JOIN FETCH cr.messages m " +
            "WHERE cr.sender = :user OR cr.receiver = :user")
    List<ChatRoomEntity> findActiveChatRoomsByUser(@Param("user") UserEntity user);

    // 특정 채팅방 ID에 해당하는 채팅방과 관련된 메시지를 패치 조인을 통해 한 번에 로드
    @Query("SELECT cr " +
            "FROM ChatRoomEntity cr " +
            "LEFT JOIN FETCH cr.messages " +
            "WHERE cr.id = :chatRoomId")
    Optional<ChatRoomEntity> findChatRoomWithMessagesById(@Param("chatRoomId") Long chatRoomId);

}