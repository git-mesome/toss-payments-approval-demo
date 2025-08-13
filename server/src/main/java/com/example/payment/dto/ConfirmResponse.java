// Toss 승인 API 응답의 일부 필드를 담는 DTO
package com.example.payment.dto;

public record ConfirmResponse(
    String paymentKey,
    String orderId,
    String orderName,
    Long totalAmount,
    String status
) {}
