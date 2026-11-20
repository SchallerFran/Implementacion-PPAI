import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// 5173 es uno de los orígenes que el backend acepta por CORS (CORS_ORIGINS en Backend/.env).
export default defineConfig({ plugins: [react()], server: { port: 5173, strictPort: true } })
