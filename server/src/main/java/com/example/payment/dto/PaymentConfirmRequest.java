package com.example.payment.dto;

/**
 * 프론트 -> 백엔드 요청 DTO
 * JSON 필드 이름과 자동 매핑됩니다.
 *
 * 예:
 * {
 *   "paymentKey": "pay_xxx",
 *   "orderId": "reservation-123",
 *   "amount": 50000
 * }
 */
public record PaymentConfirmRequest(
	String paymentKey,
	String orderId,
	Integer amount
) {}
