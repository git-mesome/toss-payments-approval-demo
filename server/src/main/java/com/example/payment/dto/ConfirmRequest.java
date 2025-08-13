// Toss 결제 승인 요청 본문
package com.example.payment.dto;

public record ConfirmRequest(
    String paymentKey,
    String orderId,
    Long amount
) {}
