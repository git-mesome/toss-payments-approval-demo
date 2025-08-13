# toss-payments-approval-demo

Toss Payments 결제 위젯 v2로 결제 요청을 하고 서버에서 승인하는 최소 예제 프로젝트입니다.
프로젝트 구조와 실행 방법을 담고 있으며 실무에서 적용할 때 유의할 점을 함께 정리했습니다.


## 개요

- `/client` 폴더에 React 프론트엔드가 있습니다.
- `/server` 폴더에 Spring Boot 백엔드가 있습니다.
- 이 예제는 DB 없이 동작하는 샘플 프로젝트입니다.
- 프론트는 결제 직전 UI에서 이미 `orderId` 와 `amount` 를 보유한다고 가정합니다.

---

## 권장 버전

아래 버전은 개발 환경에서 검증된 최소 권장 조합입니다. 실제 배포 환경에서는 각 구성요소 최신 안정 버전을 사용하세요.

- Java JDK 17 이상 권장. 가상 스레드 실험을 하려면 **JDK 21** 권장.
- Spring Boot 3.1 이상 권장. 가상 스레드 관련 기능을 쓰려면 **Spring Boot 3.2** 이상 고려.
- Node.js 18 LTS 이상
- React 18 이상
- Toss Payments SDK : v2 사용 예제

---

## 미리 준비할 것
테스트 키 또는 전자결제 신청 이후에 확인 가능한 결제위젯 연동 키를 사용하세요. 테스트 키는 토스 공식 문서에 업로드 되어있습니다.

- Toss 결제 위젯 **클라이언트 키** (프론트용)
- Toss **시크릿 키** (백엔드용)
- 로컬 개발용 `API_BASE` 주소 예: `http://localhost:8080`

---

## 실행 방법 (로컬)

1. 프론트 설치 및 실행

```bash
cd client
pnpm install
pnpm dev
```

2. 백엔드 실행

```bash
cd server
./gradlew spring-boot:run
```



## 환경 변수 예시

### 프론트 `.env` 예 (Vite 사용)

```
VITE_TOSS_CLIENT_KEY=test_gck_xxx
VITE_API_BASE_URL=http://localhost:8080
```

### 백엔드 `application.yml` 예

```yaml
toss:
  secret-key: test_gsk_xxx
server:
  port: 8080
```

**주의**: 테스트키가 아닌 시크릿 키는 절대 버전 관리 저장소에 커밋하지 마십시오.



## API 규약 요약

- 엔드포인트\
  `POST /api/payments/confirm`

- 요청 바디

```json
{
  "paymentKey": "pay_xxx",
  "orderId": "reservation-123",
  "amount": 50000
}
```

프론트에서는 `orderId` 로 `reservationId` 값을 전달합니다.

- 응답 형식 표준

```json
{
  "code": "SUCCESS" | "FAILURE",
  "data": { /* 성공 시 사용 */ } | null,
  "message": null | "사용자용 설명"
}
```

성공 시 `data` 에는 `reservationId` 와 `orderId`(동일값) 그리고 `totalAmount` 를 포함해 반환하면 프론트 호환 문제가 없습니다.




## 참고 자료
- Toss Payments 가이드\
  [https://docs.tosspayments.com/guides/v2/payment-widget/integration?frontend=react&backend=java](https://docs.tosspayments.com/guides/v2/payment-widget/integration?frontend=react&backend=java)
- Toss Payments 위젯 문서\
  [https://docs.tosspayments.com/sdk/v2/js](https://docs.tosspayments.com/sdk/v2/js)
- Toss 결제 승인 및 에러 코드 문서\
  [https://docs.tosspayments.com/reference/error-codes](https://docs.tosspayments.com/reference/error-codes)


