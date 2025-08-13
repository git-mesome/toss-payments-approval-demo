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
					// 실패 처리(요약)
					// body: 토스가 준 Map 형태 응답 (code, message 등)
					String tossCode = String.valueOf(body.getOrDefault("code", "TOSS_ERROR"));
					String tossMsg  = String.valueOf(body.getOrDefault("message", "토스 승인 실패"));

					// 1) 원본은 로그에 남긴다 (디버깅/운영용)
					// log.warn("Toss confirm failed. status={}, tossCode={}, tossMsg={}, body={}", status, tossCode, tossMsg, body);

					// 2) 사용자용 메시지로 매핑한다 (직접 보여줄 문구)
					String userMessage = mapToUserMessage(tossCode, tossMsg);

					// 3) 프론트에 일관된 형식으로 반환
					// 기존 PaymentConfirmResponse(String code, Object data, String message) 를 그대로 사용
					return new PaymentConfirmResponse("FAILURE", null, userMessage);
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

	private String mapToUserMessage(String tossCode, String tossMsg) {
		// 중요한 몇 가지 토스 코드만 명시적으로 처리
		switch (tossCode) {
			case "ALREADY_PROCESSED_PAYMENT":
				return "이미 결제가 처리되었습니다.";
			case "INVALID_API_KEY":
			case "UNAUTHORIZED_KEY":
				return "시스템 오류가 발생했습니다. 서비스 관리자에게 문의하세요.";
			case "PROVIDER_ERROR":
				return "결제사에 문제가 발생했습니다. 잠시 후 다시 시도해주세요.";
			case "REJECT_CARD_PAYMENT":
				return "카드 결제가 거절되었습니다. 카드 정보를 확인하세요.";
			case "FDS_ERROR":
				return "거래가 제한되었습니다. 고객센터에 문의하세요.";
			default:
				// 기본 fallback: 친절한 일반 안내 (원문 노출 금지)
				return "결제에 실패했습니다. 잠시 후 다시 시도하거나 고객센터에 문의하세요.";
		}
	}

}
