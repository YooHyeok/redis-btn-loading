package com.redis.button.controller;

import com.redis.button.component.SseEmitterService;
import com.redis.button.redis.component.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/redis-pubsub")
@Slf4j
@RequiredArgsConstructor
public class RedisPubSubController {
    private final RedisUtil redisUtil;
    private final SseEmitterService sseEmitterService;

    // 1. SSE 구독 API
    @GetMapping("/sse-subscribe")
    public SseEmitter sseSubscribe() {
        return sseEmitterService.subscribe();
    }


    /**
     * client 컴포넌트 진입 시점 혹은 로딩 종료시점 실행
     * 버튼 클릭여부 값이 없을 경우 false 반환
     * 버튼 클릭여부 값이 있을 경우 true 및 사용자 정보, 메뉴 조회&삭제 후 함께 반환
     * @param btn
     * @return
     */
    @GetMapping("/btn-click-time/{btn}")
    public Map<String, Object> isBtnClicked (@PathVariable String btn) {
        Map<String, Object> result = new HashMap<>();
        String clickedTime = redisUtil.getString("c-" + btn + "-time");
        result.put("isClicked", clickedTime != null);
        result.put("clickedDt", clickedTime != null ? clickedTime : "0");
        return result;
    }

    /**
     * client 컴포넌트 진입 시점 혹은 로딩 종료시점 실행
     * 버튼 클릭여부 값이 없을 경우 false 반환
     * 버튼 클릭여부 값이 있을 경우 true 및 사용자 정보, 메뉴 조회&삭제 후 함께 반환
     * @param btn
     * @return
     */
    @GetMapping("/btn-click-info/{btn}")
    public Map<String, Object> btnClickedInfo (@PathVariable String btn) {
        Map<String, Object> result = new HashMap<>();
        String detail = redisUtil.getStringWithDelete("c-detail");
        String menu = "";
        String user = "";
        if (detail != null) {
            String[] split = detail.split(":");
            menu = split[0];
            user = split[1];
        }
        result.put("menu", menu);
        result.put("user", user);
        return result;

    }

    /**
     * 버튼 클릭시 버튼 클릭여부, 메뉴 + 사용자 정보 redis 저장
     * 구독중인 모든 채널에 클릭된 버튼명:시간 정보 메시지 publish
     * @param btn
     * @param menu
     * @param user
     */
    @PostMapping("/btn-click/{btn}")
    public void btnAClick (@PathVariable String btn, String menu, String user) {
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        redisUtil.setString("c-" + btn + "-time", ""+currentTimeMillis,30L);
        redisUtil.setString("c-detail", menu + ":" + user, 40L);
        String message = btn + ":" + currentTimeMillis;
        redisUtil.publish("channel-btn-click", message);
    }


}
