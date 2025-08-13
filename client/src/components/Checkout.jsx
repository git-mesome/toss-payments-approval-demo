// 결제 버튼을 제공하고 Toss 위젯을 호출하는 컴포넌트
import { useState } from 'react';
import { loadWidget } from '../lib/toss';

export default function Checkout() {
  const [loading, setLoading] = useState(false);

  const handlePay = async () => {
    setLoading(true);
    // 1) 주문번호와 금액을 준비한다.
    const orderId = crypto.randomUUID();
    const amount = 1500; // 테스트용 금액

    // 2) Toss 결제 위젯을 로드하고 결제창을 띄운다.
    const tossPayments = await loadWidget();
    const paymentWidget = tossPayments.widgets({ customerKey: 'anonymous' });
    paymentWidget.renderPaymentMethods('#payment-method', { value: amount });
    paymentWidget.renderAgreement('#agreement');

    // 3) 결제를 요청하면 Toss가 successUrl 또는 failUrl로 이동시킨다.
    await paymentWidget.requestPayment({
      orderId,
      amount,
      orderName: '테스트 주문',
      successUrl: `${window.location.origin}/success`,
      failUrl: `${window.location.origin}/fail`,
    });
    setLoading(false);
  };

  return (
    <div>
      <h1>Toss Payments 예제</h1>
      {/* 결제수단과 약관이 렌더링될 영역 */}
      <div id="payment-method" />
      <div id="agreement" />
      <button onClick={handlePay} disabled={loading}>
        {loading ? '처리 중...' : '결제하기'}
      </button>
    </div>
  );
}
