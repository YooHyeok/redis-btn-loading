package com.redis.button.controller;

import com.redis.button.redis.component.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/redis-memmory")
@Slf4j
@RequiredArgsConstructor
public class RedisMemTTLController {
    private final RedisUtil redisUtil;

    /**
     * 버튼 클릭시 버튼 클릭여부, 메뉴 + 사용자 정보 redis 저장
     * @param btn
     * @param menu
     * @param user
     */
    @PostMapping("/btn-click/{btn}")
    public void btnAClick (@PathVariable String btn, String menu, String user) {
        redisUtil.setString("c-" + btn + "-btn", "clicked",30L);
        redisUtil.setString("c-detail", menu + ":" + user, 40L);
    }

    /**
     * [인터벌 체크]
     * 버튼 클릭여부 값이 없을 경우 false 반환
     * 버튼 클릭여부 값이 있을 경우 true 및 사용자 정보, 메뉴 조회&삭제 후 함께 반환
     * @param btn
     * @return
     */
    @GetMapping("/btn-click/{btn}")
    public Map<String, Object> isBtnClicked (@PathVariable String btn) {
        Map<String, Object> result = new HashMap<>();
        boolean exist = redisUtil.isExistString("c-" + btn + "-btn");
        result.put("isClicked", exist);
        if (!exist) { // 클릭되지 않았을 경우
            String detail = redisUtil.getStringWithDelete("c-detail");
            if (detail != null) {
                String[] split = detail.split(":");
                result.put("menu", split[0]);
                result.put("user", split[1]);
                return result;
            }
            return result;
        }
        return result;
    }
}
