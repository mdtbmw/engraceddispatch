import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { Toaster } from 'react-hot-toast'
import './index.css'
import App from './App.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Toaster
      position="top-right"
      toastOptions={{
        style: {
          background: '#08080C',
          color: '#E2E8F0',
          border: '1px solid #151522',
          borderRadius: 12,
          fontSize: 13,
        },
      }}
    />
    <App />
  </StrictMode>,
)
