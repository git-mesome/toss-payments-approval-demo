package com.example.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.payment.dto.PaymentConfirmRequest;
import com.example.payment.dto.PaymentConfirmResponse;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
public class PaymentConfirmController {

	private final PaymentService paymentService;

	public PaymentConfirmController(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	/**
	 * POST /api/payments/confirm
	 * 요청 바디로 PaymentConfirmRequest를 받음 (JSON -> 객체 자동 매핑)
	 * 응답은 Mono<ResponseEntity<PaymentConfirmResponse>> 형태로 반환
	 */
	@PostMapping("/confirm")
	public Mono<ResponseEntity<PaymentConfirmResponse>> confirm(@RequestBody PaymentConfirmRequest request) {
		return paymentService.confirmPayment(request)
			.map(ResponseEntity::ok);
	}
}