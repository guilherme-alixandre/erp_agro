function UsuarioCard({ usuario, onExcluir, isCurrentUser }) {
  return (
    <article className="animal-card usuario-card">
      <div className="usuario-card__header">
        <strong>{usuario.nome || 'Sem nome'}</strong>
        <span className="usuario-badge">{usuario.perfil}</span>
      </div>

      <p className="usuario-card__email">{usuario.email}</p>

      {!isCurrentUser ? (
        <div className="animal-card__actions">
          <button
            type="button"
            className="btn-danger"
            onClick={() => onExcluir(usuario)}
          >
            Excluir
          </button>
        </div>
      ) : (
        <p className="usuario-card__hint">Sua conta</p>
      )}
    </article>
  )
}

export default UsuarioCard
