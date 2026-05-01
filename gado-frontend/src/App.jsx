import { useEffect, useState } from 'react'
import AnimalPage from './features/animais/pages/AnimaisPage'
import PerfilPage from './features/perfil/pages/PerfilPage'
import AuthPage from './features/auth/pages/AuthPage'
import InsumosPage from './features/insumos/pages/InsumosPage'
import ConfiguracoesPage from './features/configuracoes/pages/ConfiguracoesPage'

const STORAGE_KEY = 'erp_agro_current_user'

function sanitizeUser(usuario) {
  if (!usuario || typeof usuario !== 'object') return null
  const safeUser = { ...usuario }
  delete safeUser.senha
  return safeUser
}

function App() {
  const [activePage, setActivePage] = useState('animais')
  const [currentUser, setCurrentUser] = useState(null)
  const [sessionFeedback, setSessionFeedback] = useState('')

  useEffect(() => {
    const storedUser = localStorage.getItem(STORAGE_KEY)
    if (storedUser) {
      try {
        setCurrentUser(sanitizeUser(JSON.parse(storedUser)))
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    }
  }, [])

  function handleLogin(usuario) {
    const safeUser = sanitizeUser(usuario)
    setCurrentUser(safeUser)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(safeUser))
    setActivePage('animais')
    setSessionFeedback('')
  }

  function handleLogout() {
    setCurrentUser(null)
    localStorage.removeItem(STORAGE_KEY)
    setActivePage('animais')
    setSessionFeedback('Você saiu da sessão com sucesso.')
  }

  if (!currentUser) {
    return <AuthPage onLogin={handleLogin} sessionFeedback={sessionFeedback} />
  }

  if (activePage === 'perfil') {
    return (
      <PerfilPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  if (activePage === 'insumos') {
    return (
      <InsumosPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  if (activePage === 'configuracoes' && currentUser.perfil === 'ADMINISTRADOR') {
    return (
      <ConfiguracoesPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  return (
    <AnimalPage
      currentUser={currentUser}
      onLogout={handleLogout}
      onNavigate={setActivePage}
    />
  )
}

export default App
