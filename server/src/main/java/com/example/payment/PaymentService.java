package com.example.payment;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.payment.client.TossPaymentsClient;
import com.example.payment.dto.PaymentApprovedData;
import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentConfirmResponse;

import reactor.core.publisher.Mono;

/**
 * 도메인 로직(여기선 최소: 토스 응답 정규화)
 * - Toss 응답의 status에 따라 SUCCESS / FAILURE로 변환
 * - 성공 시 PaymentApprovedData로 정규화해서 반환
 * - 실패 시 적절한 message 포함
 */
@Service
public class PaymentService {

	private final TossPaymentsClient tossClient;

	public PaymentService(TossPaymentsClient tossClient) {
		this.tossClient = tossClient;
	}

	public Mono<PaymentConfirmResponse> confirmPayment(PaymentConfirmRequest req) {
		// 요청 파라미터 검증: 필수 항목이 없으면 즉시 실패 응답
		if (req == null || req.paymentKey() == null || req.orderId() == null || req.amount() == null) {
			PaymentConfirmResponse bad = new PaymentConfirmResponse(
				"FAILURE",
				null,
				"MISSING_PARAM: paymentKey, orderId, amount 필수"
			);
			return Mono.just(bad);
		}

		return tossClient.confirmPayment(req.paymentKey(), req.orderId(), req.amount())
			.map(resp -> {
				int status = resp.getStatusCodeValue();
				Map<String, Object> body = resp.getBody() != null ? resp.getBody() : Map.of();

				if (status >= 200 && status < 300) {
					// 성공: request.orderId() == reservationId
					String reservationId = req.orderId();
					int totalAmount = extractInt(body.get("totalAmount"), req.amount());
					PaymentApprovedData data = new PaymentApprovedData(reservationId, totalAmount);
					return new PaymentConfirmResponse("SUCCESS", data, null);
				} else {
					// 실패: toss가 준 code/message를 메시지로 합쳐서 반환
					String tossCode = String.valueOf(body.getOrDefault("code", "TOSS_ERROR"));
					String tossMsg = String.valueOf(body.getOrDefault("message", "토스 승인 실패"));
					String message = tossCode + ": " + tossMsg;
					return new PaymentConfirmResponse("FAILURE", null, message);
				}
			})
			// 네트워크/타임아웃/예외 발생 시 안전한 FAILURE 반환
			.onErrorResume(ex -> {
				String msg = ex.getMessage() != null ? ex.getMessage() : "NETWORK_ERROR";
				return Mono.just(new PaymentConfirmResponse("FAILURE", null, "NETWORK_ERROR: " + msg));
			});
	}

	private int extractInt(Object v, int fallback) {
		if (v instanceof Number n) return n.intValue();
		if (v == null) return fallback;
		try { return Integer.parseInt(v.toString()); }
		catch (NumberFormatException e) { return fallback; }
	}

	@SuppressWarnings("unchecked")
	private String extractMethod(Map<String, Object> body) {
		Object m = body.get("method"); // 예: "CARD" 또는 "간편결제"
		if (m != null) return String.valueOf(m);
		Object easyPay = body.get("easyPay");
		if (easyPay instanceof Map<?,?> map) {
			Object provider = ((Map<String,Object>)map).get("provider");
			if (provider != null) return String.valueOf(provider);
		}
		return "UNKNOWN";
	}
}
