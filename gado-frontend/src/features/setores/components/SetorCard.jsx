const TIPO_LABEL = {
  PASTO: 'Pasto',
  GALPAO: 'Galpão',
  CONFINAMENTO: 'Confinamento',
  PATIO: 'Pátio',
}

function SetorCard({ setor, onDetalhes, onEditar }) {
  const isAtivo = setor.status === 'ATIVO'

  return (
    <article className="animal-card setor-card">
      <div className="setor-card__header">
        <strong>{setor.nome}</strong>
        <span className={`setor-badge ${isAtivo ? 'setor-badge--ativo' : 'setor-badge--inativo'}`}>
          {isAtivo ? 'Ativo' : 'Inativo'}
        </span>
      </div>

      <p className="setor-card__tipo">{TIPO_LABEL[setor.tipo] ?? setor.tipo}</p>

      <p className="setor-card__info">
        Capacidade: {setor.capacidadeMaxima} · {setor.lotes.length === 1 ? '1 lote' : `${setor.lotes.length} lotes`}
      </p>

      <p className="setor-card__criado">
        Criado por: <strong>{setor.criadoPorNome || setor.criadoPorEmail || '-'}</strong>
      </p>

      <div className="animal-card__actions">
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
