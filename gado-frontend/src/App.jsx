import { useState } from 'react'
import AnimaisPage from './features/animais/pages/AnimaisPage'
import LotesPage from './features/lotes/pages/LotesPage'
import PageLayout from './components/PageLayout'
import './App.css'

function App() {
  const [currentPage, setCurrentPage] = useState('animais')

  return (
      <PageLayout currentPage={currentPage} onNavigate={setCurrentPage}>
        {currentPage === 'animais' && <AnimaisPage />}
        {currentPage === 'lotes' && <LotesPage />}
      </PageLayout>
  )
}

export default App
