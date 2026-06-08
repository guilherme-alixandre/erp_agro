import { useEffect, useState } from 'react'
import AnimalPage from './features/animais/pages/AnimaisPage'
import PerfilPage from './features/perfil/pages/PerfilPage'
import AuthPage from './features/auth/pages/AuthPage'
import InsumosPage from './features/insumos/pages/InsumosPage'
import ConfiguracoesPage from './features/configuracoes/pages/ConfiguracoesPage'
import MetasPage from './features/metas/pages/MetasPage'
import LotesPage from './features/lotes/pages/LotesPage'
import SetoresPage from './features/setores/pages/SetoresPage'
import { listarSetores } from './services/setorApi'
import { listarLotesCompletos } from './services/loteApi'
import { useRefresh } from './contexts/RefreshContext.jsx'

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

  // Setores e lotes carregados uma vez após o login — compartilhados entre módulos
  const [setores, setSetores] = useState([])
  const [lotes, setLotes] = useState([])

  const { refreshGlobal } = useRefresh()

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

  // Carrega setores e lotes assim que o usuário logar; re-executa no refresh global
  useEffect(() => {
    if (!currentUser) return

    listarSetores()
      .then(setSetores)
      .catch(() => setSetores([]))

    listarLotesCompletos()
      .then(setLotes)
      .catch(() => setLotes([]))
  }, [currentUser, refreshGlobal])

  function handleLogin(usuario) {
    const safeUser = sanitizeUser(usuario)
    setCurrentUser(safeUser)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(safeUser))
    setActivePage('animais')
    setSessionFeedback('')
  }

  function handleLogout() {
    setCurrentUser(null)
    setSetores([])
    setLotes([])
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

  if (activePage === 'metas') {
    return (
      <MetasPage
        currentUser={currentUser}
        setores={setores}
        lotes={lotes}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  if (activePage === 'lotes') {
    return (
      <LotesPage
        currentUser={currentUser}
        setores={setores}
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
