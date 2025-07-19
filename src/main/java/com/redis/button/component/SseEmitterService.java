package com.redis.button.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SseEmitterService {
    /**
     * emiiters 모음
     * <pre>
     * client에서 사용자별로 sse 활성화 요청시 해당 list에 수집된다.
     * CopyOnWriteArrayList: 멀티스레드 세이프티
     * </pre>
     */
    private final List<SseEmitter> sseEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        sseEmitters.add(sseEmitter);

        sseEmitter.onCompletion(() -> sseEmitters.remove(sseEmitter));
        sseEmitter.onTimeout(() -> sseEmitters.remove(sseEmitter));

        return sseEmitter;
    }

    public void sendMessageToAllClients(String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();
        for (SseEmitter sseEmitter : sseEmitters) {
            try {
                sseEmitter.send(SseEmitter.event().data(message));
            } catch (Exception e) {
                log.error("{}", e);
                deadEmitters.add(sseEmitter);
            }

        }
        if (!deadEmitters.isEmpty()) sseEmitters.removeAll(deadEmitters);
    }
}
