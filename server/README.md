# 서버 실행 방법

Spring Boot로 구성된 간단한 결제 승인 서버입니다.

## 환경 변수 설정

```bash
export TOSS_SECRET_KEY=테스트용_시크릿키
export TOSS_API_BASE=https://api.tosspayments.com
```

## 실행

```bash
./gradlew bootRun
```

## 테스트

```bash
./gradlew test
```