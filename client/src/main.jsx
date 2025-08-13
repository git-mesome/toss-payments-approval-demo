// 앱의 진입점: 라우팅을 설정한다.
import React from 'react';
import ReactDOM from 'react-dom/client';
import {BrowserRouter, Routes, Route} from 'react-router-dom';
import Checkout from './components/Checkout';
import Success from './pages/Success';
import Fail from './pages/Fail';
import './styles/toss.css';


ReactDOM.createRoot(document.getElementById('root')).render(
    <React.StrictMode>
        {/* 간단한 라우터 구성 */}
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Checkout/>}/>
                <Route path="/success" element={<Success/>}/>
                <Route path="/fail" element={<Fail/>}/>
            </Routes>
        </BrowserRouter>
    </React.StrictMode>
);
