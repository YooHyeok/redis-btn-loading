# 서버 적용 버튼 정책

- 클릭 후 30초간 로딩바
- 다른 사용자도 로딩바 출력 공유
- 서버 적용 완료 문구 출력
  - 메뉴 정보와 사용자 정보가 일치할 경우 출력

<br>

## 방식
- ### Redis TTL & 인터벌polling
- ### Redis Pub/Sub
  - 한 화면에 여러 인터벌이 존재할 경우에 대한 대응
## Redis TTL 
<details>
<summary>접기/펼치기</summary>

### 프로세스
```text
[1] 클라이언트 버튼 클릭 → POST /redis-memmory/btn-click/{btn} + formData : menu(메뉴), user(사용자)
      
      
[2] 백엔드
    ├ Redis에 30초 TTL로 "c-{btn}-btn" = "clicked" 저장 (버튼 클릭 여부)
    └ Redis에 40초 TTL로 "c-detail" = "현재메뉴:사용자" 저장

      
[3] 클라이언트에서 30초 주기 GET /redis-memmory/btn-click/{btn} 요청 (polling)


[4] 백엔드 GET 처리
    └ Redis에서 "c-{btn}-btn" 존재 여부 확인 (버튼 클릭 여부)
         ├ 존재 → {"isClicked": true} 반환 (아직 버튼 눌림 상태)
         └ 없음 → Redis에서 "c-detail" 값을 get & delete
               ├ menu, user 분리
               └ 응답에 {"isClicked": false, "menu": menu, "user": user} 반환


[5] 클라이언트
    ├ 받은 menu, user가 현재 페이지의 메뉴, 현재 사용자와 일치하면
    │      → alert('완료되었습니다.')
    └ 아니면 아무 동작 없음
```

</details>

## Redis Pub/Sub
<details>
<summary>접기/펼치기</summary>
<br>

### 프로세스
#### 1. 화면 진입
```text
[1] 클라이언트 
    ├ (A) SSE 연결 시도 
    │   → GET /redis-pubsub/sse-subscribe
    └ (B) 버튼 클릭여부 redis 조회 
        → GET /redis-pubsub/btn-click-time/{btn}


[2] 백엔드
    ├ 1_A: SseEmitter 리스트 추가
    └ 1_B: Redis "c-{btn}-btn" key Redis 조회
         ├ 존재 → {"isClicked": true, "clickedDt":TIMESTAMP } 반환
         └ 없음 → {"isClicked": false, "clickedDt":"0" } 반환
         
         
[3] 클라이언트 - 반환시간 기준 로딩바 제어
    └ 반환여부 및 반환시간 기준 로딩바 제어
        └ 로딩바 종료(watch)시 메뉴 및 사용자정보 redis 조회  
            → GET /redis-pubsub/btn-click-info/{btn}
        
        
[4] 백엔드
    └ redis "c-detail" key Redis 조회
         ├ 없음 → 응답에 {"menu": "", "user": ""} 반환
         └ 있음 → menu, user 분리
              └ 응답에 {"menu": menu, "user": user} 반환
              
              
[5] 클라이언트 - 사용자 및 메뉴정보 일치할 경우 종료 alert
```
<br>

#### 2. 버튼 클릭 (PUB/SUB & SSE)
```text
[1] 클라이언트 - 버튼 클릭
    → POST /btn-click/{btn} + formData : menu(메뉴), user(사용자)
      
      
[2] 백엔드 - 버튼 클릭 정보 저장 및 실시간 Pub/Sub
    ├ (A) 최초 진입 조회용 버튼 클릭 정보 저장
    │    ├ Redis에 30초 TTL로 "c-{btn}-btn" = currentTimeMillis 저장
    │    └ Redis에 40초 TTL로 "c-detail" = "현재메뉴:사용자" 저장
    └ (B) Redis Pub/Sub을 통해 channel-btn-click 채널에 "{btn}:클릭시간" 메시지 발행
         └ SSE를 통해 클라이언트로 해당 메시지 전송
         
        
[3] 클라이언트 - 다른 사용자 로딩바 적용
    └ SSE(최초진입시 연결)를 통해 Redis Pub/Sub으로 발행된 메시지 수신
         └ "{btn}:클릭시간" 기준 일치하는 버튼에 로딩바 적용
               └ 로딩바 종료(watch)시 메뉴 및 사용자정보 redis 조회  
                  → GET /redis-pubsub/btn-click-info/{btn}


[4] 백엔드: 로딩바 종료 정보 요청 인입
    └ redis "c-detail" key Redis 조회
         ├ 없음 → 응답에 {"menu": "", "user": ""} 반환
         └ 있음 → menu, user 분리
              └ 응답에 {"menu": menu, "user": user} 반환
              
[5] 클라이언트 - 사용자 및 메뉴정보 일치할 경우 종료 alert
```
</details>