import { useEffect, useState } from 'react'
import AnimalPage from './features/animais/pages/AnimaisPage'
import PerfilPage from './features/perfil/pages/PerfilPage'

const STORAGE_KEY = 'erp_agro_current_user'

function App() {
  const [activePage, setActivePage] = useState('perfil')
  const [currentUser, setCurrentUser] = useState(null)

  useEffect(() => {
    const storedUser = localStorage.getItem(STORAGE_KEY)
    if (storedUser) {
      try {
        setCurrentUser(JSON.parse(storedUser))
      } catch {
        localStorage.removeItem(STORAGE_KEY)
      }
    }
  }, [])

  function handleLogin(usuario) {
    setCurrentUser(usuario)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(usuario))
    setActivePage('animais')
  }

  function handleLogout() {
    setCurrentUser(null)
    localStorage.removeItem(STORAGE_KEY)
  }

  if (activePage === 'animais') {
    return (
      <AnimalPage
        currentUser={currentUser}
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
