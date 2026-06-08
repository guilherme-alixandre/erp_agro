import '../../animais/styles/animais.css'
import '../styles/perfil.css'

function PerfilPage({ currentUser, onLogout, onNavigate }) {
  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo">🌿</div>
        <nav>
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button
              type="button"
              className="menu-item"
              onClick={() => onNavigate('lotes')}
          >
            Lotes
          </button>
          <button
              type="button"
              className="menu-item"
              onClick={() => onNavigate('setores')}
          >
            Setores
          </button>
          <button
              type="button"
              className="menu-item"
              onClick={() => onNavigate('metas')}
          >
            Metas
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('insumos')}
          >
            Insumos
          </button>
          <button type="button" className="menu-item">
            Financeiro
          </button>
          <button type="button" className="menu-item menu-item--active">
            Perfil
          </button>
          {currentUser.perfil === 'ADMINISTRADOR' ? (
            <button
              type="button"
              className="menu-item"
              onClick={() => onNavigate('configuracoes')}
            >
              ⚙ Configurações
            </button>
          ) : null}
        </nav>
        <div className="sidebar-user">
          <strong>{currentUser.nome}</strong>
          <span>{currentUser.email}</span>
          <button type="button" className="sidebar-logout" onClick={onLogout}>
            Sair
          </button>
        </div>
      </aside>

      <section className="animals-content">
        <header className="animals-header">
          <h1>Perfil</h1>
          <span>Sessão ativa: {currentUser.nome}</span>
        </header>

        <div className="perfil-stack">
          <article className="animal-card perfil-card perfil-card--main">
            <h2>Dados da conta</h2>
            <p className="perfil-subtitle">Informações da sessão atual.</p>

            <dl className="perfil-info">
              <div>
                <dt>Nome</dt>
                <dd>{currentUser.nome}</dd>
              </div>
              <div>
                <dt>E-mail</dt>
                <dd>{currentUser.email}</dd>
              </div>
              <div>
                <dt>Perfil</dt>
                <dd>{currentUser.perfil}</dd>
              </div>
            </dl>

            <div className="modal-actions perfil-actions">
              <button type="button" className="btn-secondary" onClick={onLogout}>
                Sair
              </button>
            </div>
          </article>
        </div>
      </section>
    </main>
  )
}

export default PerfilPage
