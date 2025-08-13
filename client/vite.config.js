import {defineConfig} from 'vite';
import react from '@vitejs/plugin-react';

// Vite 기본 설정에 React 플러그인 추가
export default defineConfig({
    plugins: [react()],
    server: {
        proxy: {
            '/api': 'http://localhost:8080'
        }
    }
});