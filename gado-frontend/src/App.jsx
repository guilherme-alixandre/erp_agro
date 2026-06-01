import { useEffect, useState } from 'react'
import AnimaisPage from './features/animais/pages/AnimaisPage'
import LotesPage from './features/lotes/pages/LotesPage'
import SetoresPage from './features/setores/pages/SetoresPage'
import PerfilPage from './features/perfil/pages/PerfilPage'
import AuthPage from './features/auth/pages/AuthPage'
import PageLayout from './components/PageLayout'

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

  let pageContent = null

  if (activePage === 'animais') {
    pageContent = <AnimaisPage currentUser={currentUser} />
  } else if (activePage === 'lotes') {
    pageContent = <LotesPage currentUser={currentUser} />
  } else if (activePage === 'setores') {
    pageContent = <SetoresPage currentUser={currentUser} />
  } else if (activePage === 'perfil') {
    pageContent = (
      <PerfilPage
        currentUser={currentUser}
        onLogout={handleLogout}
      />
    )
  }

  return (
    <PageLayout
      currentUser={currentUser}
      currentPage={activePage}
      onNavigate={setActivePage}
      onLogout={handleLogout}
    >
      {pageContent}
    </PageLayout>
  )
}

export default App
