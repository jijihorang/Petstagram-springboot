package com.petstagram.controller;

import com.petstagram.dto.NotificationDTO;
import com.petstagram.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/subscribe", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter subscribe(@RequestParam Long userId, @RequestParam String token) {
        return notificationService.subscribe(userId);
    }

    @GetMapping("/notifications/{userId}")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByUserId(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.findByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

}