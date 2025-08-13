import {loadTossPayments, ANONYMOUS} from "@tosspayments/tosspayments-sdk";
import {useEffect, useState} from "react";

// 토스페이먼츠 SDK를 초기화할 때 필요한 클라이언트 키를 환경 변수에서 읽는다
// 개발 환경에서는 VITE_TOSS_CLIENT_KEY를 .env 파일에 정의한다
// @docs https://docs.tosspayments.com/sdk/v2/js#토스페이먼츠-초기화
const clientKey = import.meta.env.VITE_TOSS_CLIENT_KEY;

// 구매자 식별에 사용할 customerKey를 준비한다
// 실제 서비스에서는 서버가 로그인 사용자의 고유 식별자 UUID를 내려준다
// 비회원 결제는 ANONYMOUS를 사용한다
// @docs https://docs.tosspayments.com/sdk/v2/js#tosspaymentswidgets
// TODO : 실제 서비스에서는 로그인 사용자 식별자로 서버가 내려준 UUID 값을 customerKey에 설정합니다.
const customerKey = generateRandomString();

// 데모 전용 예약 정보(= orderId, amount)
// 실제 서비스에선 화면 상태나 props로 받은 reservationId/totalPrice를 사용
const DEMO_RESERVATION_ID = `demo-resv-${generateRandomString()}`; // 토스 orderId로 사용
const DEMO_TOTAL_PRICE = 10000; // KRW

export default function CheckoutPage() {

    // 결제 금액 정보
    const [amount, setAmount] = useState({
        currency: "KRW",
        value: DEMO_TOTAL_PRICE,
    });

    // 위젯 렌더링 완료 여부, 버튼 비활성화 처리
    const [ready, setReady] = useState(false);
    // 위젯은 SDK가 만들어 주는 외부 객체이므로 state로 관리하기보다 이렇게 참조를 저장해 둔다
    const [widgets, setWidgets] = useState(null);

    // 1단계 SDK 초기화와 위젯 생성
    useEffect(() => {
        async function fetchPaymentWidgets() {
            try {
                // ------  SDK 초기화 ------
                // @docs https://docs.tosspayments.com/sdk/v2/js#토스페이먼츠-초기화
                const tossPayments = await loadTossPayments(clientKey);

                // 회원 결제
                // @docs https://docs.tosspayments.com/sdk/v2/js#tosspaymentswidgets
                const widgets = tossPayments.widgets({
                    customerKey,
                });
                // 비회원 결제
                // const widgets = tossPayments.widgets({ customerKey: ANONYMOUS });

                // 생성된 위젯을 상태에 보관한다
                setWidgets(widgets);
            } catch (error) {
                console.error("Error fetching payment widget:", error);
            }
        }

        fetchPaymentWidgets();
    }, [clientKey, customerKey]);

    // 2단계 결제 금액 설정과 UI 렌더링
    useEffect(() => {
        async function renderPaymentWidgets() {
            if (widgets == null) {
                return;
            }

            // ------  주문서의 결제 금액 설정 ------
            // TODO: renderPaymentMethods, renderAgreement, requestPayment 보다 반드시 선행되어야 합니다.
            await widgets.setAmount(amount);

            // ------  결제 UI 렌더링 ------
            // @docs https://docs.tosspayments.com/sdk/v2/js#widgetsrenderpaymentmethods
            await widgets.renderPaymentMethods({
                selector: "#payment-method",
                // 렌더링하고 싶은 결제 UI의 variantKey
                // 결제 수단 및 스타일이 다른 멀티 UI를 직접 만들고 싶다면 계약이 필요하다.
                // @docs https://docs.tosspayments.com/guides/v2/payment-widget/admin#새로운-결제-ui-추가하기
                variantKey: "DEFAULT",
            });

            // ------  이용약관 UI 렌더링 ------
            // @docs https://docs.tosspayments.com/reference/widget-sdk#renderagreement선택자-옵션
            await widgets.renderAgreement({
                selector: "#agreement",
                variantKey: "AGREEMENT",
            });

            setReady(true);
        }

        renderPaymentWidgets();
    }, [widgets]);

    return (
        <div className="wrapper">
            <div className="box_section">
                {/* 결제 UI */}
                <div id="payment-method"/>
                {/* 이용약관 UI */}
                <div id="agreement"/>

                {/* 결제하기 버튼 */}
                <button
                    className="button"
                    style={{marginTop: "30px"}}
                    disabled={!ready}
                    // ------ '결제하기' 버튼 누르면 결제창 띄우기 ------
                    // @docs https://docs.tosspayments.com/sdk/v2/js#widgetsrequestpayment
                    onClick={async () => {
                        try {
                            await widgets.requestPayment({
                                // 필수 orderId, successUrl, failUrl 외 선택사항
                                orderId: DEMO_RESERVATION_ID,
                                orderName: "공연 좌석 티켓",
                                successUrl: window.location.origin + "/success",
                                failUrl: window.location.origin + "/fail",
                                customerEmail: "customer123@gmail.com",
                                customerName: "김토스",
                                customerMobilePhone: "01012341234",
                            });
                        } catch (error) {
                            // 에러 처리하기
                            console.error(error);
                        }
                    }}
                >
                    결제하기
                </button>

                {/* 값 확인용 : 확인했으면 삭제 부탁드립니다 */}
                <p style={{ marginTop: 12 }}>
                    데모 예약 ID(orderId): {DEMO_RESERVATION_ID}
                    <br />
                    결제 금액: {amount.value.toLocaleString()} {amount.currency}
                </p>

            </div>
        </div>
    );
}


// 브라우저에서 간단한 랜덤 문자열 생성(데모 전용)
// 실제 서비스에서는 서버가 안전한 식별자를 발급
function generateRandomString() {
    return window.btoa(Math.random().toString()).slice(0, 20);
}
