package com.example.payment.client;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

/**
 * WebClient 기반 Toss Payments 호출 전담 클래스 (논블로킹)
 * - 생성자에서 시크릿 키를 받아 Authorization 헤더를 기본 설정으로 둠
 * - confirmPayment은 토스의 응답을 ResponseEntity<Map<String,Object>> 형태로 반환
 */
@Component
public class TossPaymentsClient {

	private final WebClient webClient;

	/**
	 * WebClient.Builder를 주입받아 사용.
	 * toss.secret-key는 application.yml 또는 환경변수로 설정되어야 함.
	 */
	public TossPaymentsClient(WebClient.Builder builder,
		@Value("${toss.secret-key}") String secretKey) {
		if (secretKey == null || secretKey.isBlank()) {
			throw new IllegalStateException("toss.secret-key가 설정되어 있지 않습니다.");
		}
		String encoded = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		this.webClient = builder
			.baseUrl("https://api.tosspayments.com")
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
			.build();
	}

	/**
	 * 토스 결제 승인 API 호출 (논블로킹)
	 *
	 * @param paymentKey 토스에서 발급한 paymentKey
	 * @param orderId    우리 서비스의 reservationId 값을 토스의 orderId로 전달
	 * @param amount     결제 금액 (원 단위)
	 * @return Mono<ResponseEntity<Map<String,Object>>> (HTTP 상태 + body)
	 */
	public Mono<ResponseEntity<Map<String, Object>>> confirmPayment(String paymentKey, String orderId, int amount) {
		Map<String, Object> body = Map.of(
			"paymentKey", paymentKey,
			"orderId", orderId,
			"amount", amount
		);

		return webClient.post()
			.uri("/v1/payments/confirm")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(body)
			// exchangeToMono을 사용해 ResponseEntity<Map> 형태로 받음 (4xx/5xx의 body도 읽을 수 있음)
			.exchangeToMono(resp -> resp.toEntity(new ParameterizedTypeReference<Map<String, Object>>() {}))
			// 네트워크 문제 등 무한대기 방지
			.timeout(Duration.ofSeconds(10));
	}
}
