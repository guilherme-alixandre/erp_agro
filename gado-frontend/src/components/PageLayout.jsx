import Sidebar from './Sidebar'

function PageLayout({ currentUser, currentPage, onNavigate, onLogout, children }) {
  return (
    <main className="animals-layout">
      <Sidebar
        currentUser={currentUser}
        currentPage={currentPage}
        onNavigate={onNavigate}
        onLogout={onLogout}
      />
      {children}
    </main>
  )
}

export default PageLayout
