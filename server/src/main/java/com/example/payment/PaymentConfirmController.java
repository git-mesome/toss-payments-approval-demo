package com.example.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/payments")
public class PaymentConfirmController {

	@Value("${toss.secret-key}")
	private String secretKey;

	@PostMapping("/confirm")
	public ResponseEntity<?> confirm(@RequestBody Map<String, Object> request) {
		String paymentKey = (String)request.get("paymentKey");
		String orderId = (String)request.get("orderId");
		int amount = (int)request.get("amount");

		System.out.printf("📌 [결제 승인 요청] paymentKey=%s, orderId=%s, amount=%d%n",
			paymentKey, orderId, amount);

		String url = "https://api.tosspayments.com/v1/payments/confirm";

		// Toss Payments Basic 인증 헤더 생성
		String encodedAuth = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Basic " + encodedAuth);
		headers.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> body = Map.of(
			"paymentKey", paymentKey,
			"orderId", orderId,
			"amount", amount
		);

		RestTemplate restTemplate = new RestTemplate();
		// ✅ Toss 응답이 4xx/5xx여도 예외를 던지지 않게 처리
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
				return false; // HTTP status가 에러여도 예외로 처리하지 않음
			}
		});

		try {
			ResponseEntity<Map> tossResponse = restTemplate.postForEntity(
				url, new HttpEntity<>(body, headers), Map.class
			);

			return ResponseEntity.ok(Map.of(
				"code", "SUCCESS",
				"data", tossResponse.getBody()
			));
		} catch (HttpClientErrorException e) {
			// Toss API에서 내려준 에러 응답을 그대로 파싱
			String errorBody = e.getResponseBodyAsString();
			return ResponseEntity
				.status(e.getStatusCode())
				.body(Map.of(
					"code", "FAILURE",
					"error", errorBody
				));
		} catch (Exception e) {
			return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of(
					"code", "FAILURE",
					"message", e.getMessage()
				));
		}
	}
}
