// dto/PaymentApprovedData.java
package com.example.payment.dto;

public record PaymentApprovedData(
	String reservationId,
	int totalAmount
) {
}
