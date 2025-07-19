# 서버 적용 버튼 정책

- 클릭 후 30초간 로딩바
- 다른 사용자도 로딩바 출력 공유
- 서버 적용 완료 문구 출력
  - 메뉴 정보와 사용자 정보가 일치할 경우 출력

<br>

## 방식
- ### Redis TTL & 인터벌polling
- ### Redis Pub/Sub

## Redis TTL 
<details>
<summary>접기/펼치기</summary>

### 프로세스
```text
[1] 클라이언트 버튼 클릭 → POST /btn-click/{btn} + formData : menu(메뉴), user(사용자)
      
      
[2] 백엔드
    ├ Redis에 30초 TTL로 "c-{btn}-btn" = "clicked" 저장 (버튼 클릭 여부)
    └ Redis에 40초 TTL로 "c-detail" = "현재메뉴:사용자" 저장

      
[3] 클라이언트에서 30초 주기 GET /btn-click/{btn} 요청 (polling)


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
