package com.redis.button.redis.component;

import com.redis.button.component.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ButtonClickMessageListener implements MessageListener {
    private final SseEmitterService sseEmitterService;

    /**
     * redis pub 발행된 메시지를 수신.
     * 백엔드 상에서 수신되면 바로 sse를 통해 sse가 활성화된 모든 사용자에게 전송
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String convertMessage = new String(message.getBody(), StandardCharsets.UTF_8);
        sseEmitterService.sendMessageToAllClients(convertMessage);
    }
}
