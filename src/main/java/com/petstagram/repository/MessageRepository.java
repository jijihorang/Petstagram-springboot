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
}