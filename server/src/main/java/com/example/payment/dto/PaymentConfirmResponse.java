// dto/PaymentConfirmResponse.java
package com.example.payment.dto;

public record PaymentConfirmResponse(
	String code,
	PaymentApprovedData data,
	String message
) {}