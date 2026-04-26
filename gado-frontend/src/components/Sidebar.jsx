function Sidebar({ currentPage, onNavigate }) {
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
        <button type="button" className="menu-item">
          Setores
        </button>
        <button type="button" className="menu-item">
          Insumos
        </button>
        <button type="button" className="menu-item">
          Financeiro
        </button>
        <button type="button" className="menu-item">
          Perfil
        </button>
      </nav>
    </aside>
  )
}

export default Sidebar
