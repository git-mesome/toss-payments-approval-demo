// Toss 결제 위젯 로더를 분리하여 관리한다.
import { loadTossPayments } from '@tosspayments/payment-widget-sdk';

/**
 * 클라이언트 키를 사용하여 Toss Payments 객체를 로드한다.
 */
export async function loadWidget() {
  const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY;
  return await loadTossPayments(clientKey);
}
