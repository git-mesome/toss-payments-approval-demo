import {useSearchParams} from "react-router-dom";

export default function FailPage() {
    const [searchParams] = useSearchParams();

    const message = searchParams.get("message") || "알 수 없는 오류";
    const code = searchParams.get("code") || "-";

    return (
        <div id="info" className="box_section" style={{width: "600px", margin: "0 auto", textAlign: "center"}}>
            <img
                width="100px"
                src="https://static.toss.im/lotties/error-spot-no-loop-space-apng.png"
                alt="에러 이미지"
                style={{marginBottom: "16px"}}
            />
            <h2>결제를 실패했어요</h2>

            <div className="p-grid typography--p" style={{marginTop: "50px"}}>
                <div className="p-grid-col text--left">
                    <b>에러메시지</b>
                </div>
                <div className="p-grid-col text--right" id="message">{message}</div>
            </div>
            <div className="p-grid typography--p" style={{marginTop: "10px"}}>
                <div className="p-grid-col text--left">
                    <b>에러코드</b>
                </div>
                <div className="p-grid-col text--right" id="code">{code}</div>
            </div>

            <div className="p-grid-col" style={{marginTop: "24px"}}>
                <a
                    href="https://docs.tosspayments.com/guides/v2/payment-widget/integration"
                    target="_blank"
                    rel="noopener noreferrer"
                >
                    <button className="button p-grid-col5">연동 문서</button>
                </a>
                <a
                    href="https://discord.gg/A4fRFXQhRu"
                    target="_blank"
                    rel="noopener noreferrer"
                    style={{marginLeft: "8px"}}
                >
                    <button
                        className="button p-grid-col5"
                        style={{backgroundColor: "#e8f3ff", color: "#1b64da"}}
                    >
                        실시간 문의
                    </button>
                </a>
            </div>
        </div>
    );
}
