import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Estilos base primeiro: o sistema visual importado pelo App precisa vir depois para prevalecer.
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
