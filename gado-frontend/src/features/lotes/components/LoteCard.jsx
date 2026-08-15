function LoteCard({ lote, onDetalhes, onEditar }) {
  const isAtivo = lote.statusLote === 'ATIVO'

  const setoresLabel = lote.alocacoes.length === 1 ? '1 setor' : `${lote.alocacoes.length} setores`
  const animaisLabel = lote.totalAnimais === 1 ? '1 animal' : `${lote.totalAnimais} animais`

  return (
    <article className="animal-card lote-card">
      <div className="lote-card__header">
        <span className="lote-card__codigo">{lote.codigo}</span>
        <span className={`lote-card__status ${isAtivo ? 'lote-card__status--ativo' : 'lote-card__status--inativo'}`}>
          {isAtivo ? 'Ativo' : 'Inativo'}
        </span>
      </div>

      <p className="lote-card__cor">{lote.corBrinco || 'Sem cor'}</p>

      <p className="lote-card__info">
        {animaisLabel} · {setoresLabel}
      </p>

      <p className="lote-card__criado">
        Criado por: <strong>{lote.criadoPorNome || lote.criadoPorEmail || '-'}</strong>
      </p>

      <div className="animal-card__actions">
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
