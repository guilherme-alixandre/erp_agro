import Sidebar from './Sidebar'

function PageLayout({ currentPage, onNavigate, children }) {
  return (
    <div className="animals-layout">
      <Sidebar currentPage={currentPage} onNavigate={onNavigate} />
      {children}
    </div>
  )
}

export default PageLayout
