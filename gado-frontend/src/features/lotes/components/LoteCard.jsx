function LoteCard({ lote, onDetalhes, onEditar }) {
  const isAtivo = lote.statusLote === 'ATIVO'
  const statusClass = isAtivo ? 'lote-card__status--ativo' : 'lote-card__status--inativo'
  const statusLabel = isAtivo ? 'Ativo' : 'Inativo'

  const setoresLabel =
    lote.alocacoes.length === 1
      ? '1 setor'
      : `${lote.alocacoes.length} setores`

  const animaisLabel =
    lote.totalAnimais === 1
      ? '1 animal'
      : `${lote.totalAnimais} animais`

  return (
    <article className="lote-card">
      <div className="lote-card__top">
        <span className="lote-card__codigo">{lote.codigo}</span>
        <span className={`lote-card__status ${statusClass}`}>{statusLabel}</span>
      </div>

      <div className="lote-card__cor">
        <strong>{lote.corBrinco || 'Sem cor'}</strong>
      </div>

      <div className="lote-card__info">
        <span>{animaisLabel}</span>
        <span>{setoresLabel}</span>
      </div>

      <p className="lote-card__criado">
        Criado por:{' '}
        <strong>{lote.criadoPorNome || lote.criadoPorEmail || '-'}</strong>
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
