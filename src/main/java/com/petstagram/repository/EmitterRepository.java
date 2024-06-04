package com.petstagram.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Map;

@Repository
public class EmitterRepository {
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> sseEmitters = new ConcurrentHashMap<>();

    public void addEmitter(Long userId, SseEmitter emitter) {
        sseEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
    }

    public void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = sseEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                sseEmitters.remove(userId);
            }
        }
    }

    public CopyOnWriteArrayList<SseEmitter> getEmitters(Long userId) {
        return sseEmitters.get(userId);
    }

    public Map<Long, CopyOnWriteArrayList<SseEmitter>> getAllEmitters() {
        return sseEmitters;
    }
}
