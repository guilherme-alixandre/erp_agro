import { useEffect, useState } from 'react'
import { setAuthToken, setUnauthorizedHandler } from './integration/apiClient'
import AnimalPage from './features/animais/pages/AnimaisPage'
import PerfilPage from './features/perfil/pages/PerfilPage'
import AuthPage from './features/auth/pages/AuthPage'
import InsumosPage from './features/insumos/pages/InsumosPage'
import FinanceiroPage from './features/financeiro/pages/FinanceiroPage'
import ConfiguracoesPage from './features/configuracoes/pages/ConfiguracoesPage'
import SetoresPage from './features/setores/pages/SetoresPage'
import LotesPage from './features/lotes/pages/LotesPage'
import MetasPage from './features/metas/pages/MetasPage'
import TarefasPage from './features/tarefas/pages/TarefasPage'

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
    setUnauthorizedHandler(() => handleLogout('Sua sessão expirou. Faça login novamente.'))

    const storedUser = localStorage.getItem(STORAGE_KEY)
    if (storedUser) {
      try {
        setCurrentUser(sanitizeUser(JSON.parse(storedUser)))
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    }
  }, [])

  function handleLogin(usuario, token) {
    const safeUser = sanitizeUser(usuario)
    setAuthToken(token)
    setCurrentUser(safeUser)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(safeUser))
    setActivePage('animais')
    setSessionFeedback('')
  }

  function handleUpdateUser(updatedUser) {
    const safeUser = sanitizeUser(updatedUser)
    setCurrentUser(safeUser)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(safeUser))
  }

  function handleLogout(message) {
    setAuthToken(null)
    setCurrentUser(null)
    localStorage.removeItem(STORAGE_KEY)
    setActivePage('animais')
    setSessionFeedback(typeof message === 'string' ? message : 'Você saiu da sessão com sucesso.')
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
        onUpdateUser={handleUpdateUser}
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

  if (activePage === 'financeiro' && ['ADMINISTRADOR', 'GERENTE', 'FINANCEIRO'].includes(currentUser.perfil)) {
    return (
      <FinanceiroPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  if (activePage === 'metas') {
    return (
      <MetasPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  if (activePage === 'lotes') {
    return (
      <LotesPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  if (activePage === 'setores') {
    return (
      <SetoresPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  if (activePage === 'tarefas') {
    return (
      <TarefasPage
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
        onUpdateUser={handleUpdateUser}
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
