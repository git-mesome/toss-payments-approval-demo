// 결제 승인 요청을 받아 TossApprovalClient로 전달하는 컨트롤러
package com.example.payment;

import com.example.payment.dto.ConfirmRequest;
import com.example.payment.dto.ConfirmResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "http://localhost:5173") // 프론트에서 접근 허용
public class ConfirmController {
  private final TossApprovalClient client;

  public ConfirmController(TossApprovalClient client) {
    this.client = client;
  }

  @PostMapping("/confirm")
  public ResponseEntity<?> confirm(@RequestBody ConfirmRequest request) {
    try {
      // 서버에서 Toss 승인 API를 호출하여 응답을 전달한다.
      ConfirmResponse response = client.confirm(request);
      return ResponseEntity.ok(response);
    } catch (WebClientResponseException e) {
      // Toss에서 내려준 에러 바디를 그대로 반환한다.
      return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
    }
  }
}
