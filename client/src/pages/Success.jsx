import {useEffect, useRef, useState} from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

export default function SuccessPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const [paymentData, setPaymentData] = useState(null);
    const calledRef = useRef(false); // <StrictMode> 로 인한 중복 호출 방지

     const API_BASE = import.meta.env.VITE_API_BASE_URL;

    useEffect(() => {
        if (calledRef.current) return;
        calledRef.current = true;

        // 1) 토스가 successUrl에 붙여준 쿼리 파라미터 읽기
        const paymentKey = searchParams.get("paymentKey");
        const reservationId = searchParams.get("orderId");
        const amount = searchParams.get("amount");

        // 2) 필수값 확인  하나라도 없으면 실패 페이지로 이동
        if (!paymentKey || !reservationId || !amount) {
            navigate("/fail?code=MISSING_PARAM&message=필수 파라미터 누락");
            return;
        }

        console.log("📌 [프론트] 결제 승인 API 호출 시작", {
            paymentKey,
            reservationId,
            amount: amount,
            timestamp: new Date().toISOString()
        });

        // 3) 백엔드 서버로 결제 승인 요청
        async function confirmPayment() {
            try {
                const res = await fetch(`${API_BASE}/api/payments/confirm`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ paymentKey, reservationId, amount: Number(amount) }),
                });

                console.log("📌 [프론트] 결제 승인 API 응답 상태", res.status);
                const json = await res.json();
                console.log("📌 [프론트] 결제 승인 API 응답 데이터", json);
                // 백엔드에서 code로 성공/실패 구분
                if (json.code !== "SUCCESS") {
                    throw { code: json.code, message: json.message };
                }

                // 성공 시 결제 데이터만 저장
                setPaymentData(json.data);

            } catch (err) {
                console.error("❌ [프론트] 결제 승인 실패", err);
                navigate(`/fail?code=${err.code || "UNKNOWN"}&message=${err.message || "승인 실패"}`);
            }
        }

        confirmPayment();
    }, [searchParams, API_BASE, navigate]);

    return (
        <div className="box_section" style={{ width: "600px" }}>
            {paymentData ? (
                <>
                    <h2>결제가 완료되었습니다 🎉</h2>
                    <p>주문 번호: {paymentData.reservationId}</p>
                    <p>결제 금액: {paymentData.totalAmount}원</p>
                    <p>결제 수단: {paymentData.method}</p>
                </>
            ) : (
                <p>승인 요청 중...</p>
            )}
        </div>
    );
}
