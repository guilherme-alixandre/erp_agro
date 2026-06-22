const TIPO_LABEL = {
  PASTO: 'Pasto',
  GALPAO: 'Galpão',
  CONFINAMENTO: 'Confinamento',
  PATIO: 'Pátio',
}

function SetorCard({ setor, onDetalhes, onEditar }) {
  const isAtivo = setor.status === 'ATIVO'
  const statusClass = isAtivo
    ? 'setor-card-item__status--ativo'
    : 'setor-card-item__status--inativo'
  const statusLabel = isAtivo ? 'Ativo' : 'Inativo'

  return (
    <article className="setor-card-item">
      <div className="setor-card-item__top">
        <span className="setor-card-item__nome">{setor.nome}</span>
        <span className={`setor-card-item__status ${statusClass}`}>
          {statusLabel}
        </span>
      </div>

      <div className="setor-card-item__tipo">
        <strong>{TIPO_LABEL[setor.tipo] ?? setor.tipo}</strong>
      </div>

      <div className="setor-card-item__info">
        <span>Capacidade: {setor.capacidadeMaxima}</span>
        <span>
          {setor.lotes.length === 1
            ? '1 lote'
            : `${setor.lotes.length} lotes`}
        </span>
      </div>

      <p className="setor-card-item__criado">
        Criado por:{' '}
        <strong>{setor.criadoPorNome || setor.criadoPorEmail || '-'}</strong>
      </p>

      <div className="setor-card-item__actions">
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
