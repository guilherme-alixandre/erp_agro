import '../../animais/styles/animais.css'
import '../styles/perfil.css'

function PerfilPage({ currentUser, onLogout }) {
  return (
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
  )
}

export default PerfilPage
