function Sidebar({ currentUser, currentPage, onNavigate, onLogout }) {
  return (
    <aside className="animals-sidebar">
      <div className="animals-logo">🌿</div>
      <nav>
        <button
          type="button"
          className={`menu-item ${currentPage === 'animais' ? 'menu-item--active' : ''}`}
          onClick={() => onNavigate('animais')}
        >
          Animais
        </button>
        <button
          type="button"
          className={`menu-item ${currentPage === 'lotes' ? 'menu-item--active' : ''}`}
          onClick={() => onNavigate('lotes')}
        >
          Lotes
        </button>
        <button
          type="button"
          className={`menu-item ${currentPage === 'setores' ? 'menu-item--active' : ''}`}
          onClick={() => onNavigate('setores')}
        >
          Setores
        </button>
        <button type="button" className="menu-item">
          Insumos
        </button>
        <button type="button" className="menu-item">
          Financeiro
        </button>
        <button
          type="button"
          className={`menu-item ${currentPage === 'perfil' ? 'menu-item--active' : ''}`}
          onClick={() => onNavigate('perfil')}
        >
          Perfil
        </button>
      </nav>
      <div className="sidebar-user">
        <strong>{currentUser.nome}</strong>
        <span>{currentUser.email}</span>
        <button type="button" className="sidebar-logout" onClick={onLogout}>
          Sair
        </button>
      </div>
    </aside>
  )
}

export default Sidebar
