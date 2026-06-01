function SetorCard({ setor, onDetalhes, onEditar }) {
  return (
    <article className="setor-card">
      <div className="setor-card__top">
        <span>ID: {setor.id}</span>
        <span>Tipo: {setor.setor}</span>
      </div>

      <div className="setor-card__middle">
        <strong>{setor.nome || 'Sem nome'}</strong>
        <strong>Capacidade: {setor.capacidadeMaxima} animais</strong>
      </div>

      <p className="setor-card__vaccines">
        Meta: {setor.metaTexto || 'Não informada'}
      </p>

      <div className="setor-card__actions">
        <button type="button" onClick={() => onDetalhes(setor)}>
          Detalhes
        </button>
        <button type="button" onClick={() => onEditar(setor)}>
          Editar
        </button>
      </div>
    </article>
  )
}

export default SetorCard
