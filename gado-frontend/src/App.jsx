import { useEffect, useState } from 'react'
import AnimalPage from './features/animais/pages/AnimaisPage'
import PerfilPage from './features/perfil/pages/PerfilPage'

const STORAGE_KEY = 'erp_agro_current_user'

function sanitizeUser(usuario) {
  if (!usuario || typeof usuario !== 'object') return null
  const safeUser = { ...usuario }
  delete safeUser.senha
  return safeUser
}

function App() {
  const [activePage, setActivePage] = useState('perfil')
  const [currentUser, setCurrentUser] = useState(null)

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
  }

  function handleLogout() {
    setCurrentUser(null)
    localStorage.removeItem(STORAGE_KEY)
    setActivePage('perfil')
  }

  if (activePage === 'animais') {
    return (
      <AnimalPage
        currentUser={currentUser}
        onLogout={handleLogout}
        onNavigate={setActivePage}
      />
    )
  }

  return (
    <PerfilPage
      currentUser={currentUser}
      onLogin={handleLogin}
      onLogout={handleLogout}
      onNavigate={setActivePage}
    />
  )
}

export default App
