// 서버에서 Toss 승인 API를 호출하는 클라이언트
package com.example.payment;

import com.example.payment.dto.ConfirmRequest;
import com.example.payment.dto.ConfirmResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TossApprovalClient {
  private final WebClient webClient;

  public TossApprovalClient(
      @Value("${TOSS_API_BASE:https://api.tosspayments.com}") String apiBase,
      @Value("${TOSS_SECRET_KEY}") String secretKey) {
    String basicAuth = Base64.getEncoder()
        .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    this.webClient = WebClient.builder()
        .baseUrl(apiBase)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }

  /**
   * Toss 결제 승인 API를 호출한다. 이 과정은 서버에서만 수행되어야 하므로
   * 시크릿 키가 프론트에 노출되지 않는다.
   */
  public ConfirmResponse confirm(ConfirmRequest request) {
    return webClient.post()
        .uri("/v1/payments/confirm")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(ConfirmResponse.class)
        .block();
  }
}
