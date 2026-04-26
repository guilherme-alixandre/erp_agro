function LoteCard({ lote, onDetalhes, onEditar }) {
  return (
    <article className="lote-card">
      <div className="lote-card__top">
        <span>ID: {lote.id}</span>
        <span>Usuário</span>
      </div>

      <div className="lote-card__middle">
        <strong>{lote.descricao || 'Sem descrição'}</strong>
        <strong>{lote.racaPredominante || 'Raça não informada'}</strong>
      </div>

      <p className="lote-card__info">
        Usuário ID: {lote.usuario_id || 'Não informado'}
      </p>

      <div className="lote-card__actions">
        <button type="button" onClick={() => onDetalhes(lote)}>
          Detalhes
        </button>
        <button type="button" onClick={() => onEditar(lote)}>
          Editar
        </button>
      </div>
    </article>
  )
}

export default LoteCard
