const TIPO_LABEL = {
  PASTO: 'Pasto',
  GALPAO: 'Galpão',
  CONFINAMENTO: 'Confinamento',
  PATIO: 'Pátio',
}

function SetorDetailsModal({ setor, onClose, onEdit, onDelete, isDeleting }) {
  const ocupacaoTotal = setor.lotes.reduce(
    (acc, l) => acc + (l.quantidadeAnimais ?? 0),
    0,
  )

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>Detalhes do setor</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <dl className="details-grid">
          <div>
            <dt>Nome</dt>
            <dd>{setor.nome}</dd>
          </div>
          <div>
            <dt>Tipo</dt>
            <dd>{TIPO_LABEL[setor.tipo] ?? setor.tipo ?? '-'}</dd>
          </div>
          <div>
            <dt>Status</dt>
            <dd>{setor.status === 'ATIVO' ? 'Ativo' : 'Inativo'}</dd>
          </div>
          <div>
            <dt>Capacidade máxima</dt>
            <dd>{setor.capacidadeMaxima} animais</dd>
          </div>
          <div>
            <dt>Ocupação atual</dt>
            <dd>
              {ocupacaoTotal}/{setor.capacidadeMaxima} animais (
              {setor.lotes.length}{' '}
              {setor.lotes.length === 1 ? 'lote' : 'lotes'})
            </dd>
          </div>
          {setor.metaTexto ? (
            <div className="details-grid__full">
              <dt>Observações</dt>
              <dd>{setor.metaTexto}</dd>
            </div>
          ) : null}
          <div>
            <dt>Criado por</dt>
            <dd>{setor.criadoPorNome || setor.criadoPorEmail || '-'}</dd>
          </div>
          {setor.alteradoPorNome || setor.alteradoPorEmail ? (
            <div>
              <dt>Alterado por</dt>
              <dd>{setor.alteradoPorNome || setor.alteradoPorEmail}</dd>
            </div>
          ) : null}
        </dl>

        <h3 className="details-section">Lotes neste setor</h3>

        {setor.lotes.length === 0 ? (
          <p className="vacinas-empty">Nenhum lote alocado neste setor.</p>
        ) : (
          <div className="setor-lotes">
            {setor.lotes.map((l) => (
              <div
                key={l.loteSectorId ?? l.loteId}
                className="setor-lote-item"
              >
                <div className="setor-lote-item__header">
                  <strong>{l.loteCodigo || `Lote #${l.loteId}`}</strong>
                  {l.loteCorBrinco ? (
                    <span className="setor-lote-item__cor">
                      {l.loteCorBrinco}
                    </span>
                  ) : null}
                  <span className="setor-lote-item__animais">
                    {l.quantidadeAnimais}{' '}
                    {l.quantidadeAnimais === 1 ? 'animal' : 'animais'}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}

        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={onEdit}>
            Editar
          </button>
          <button
            type="button"
            className="btn-danger"
            onClick={() => onDelete(setor)}
            disabled={isDeleting}
          >
            {isDeleting ? 'Excluindo...' : 'Excluir'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default SetorDetailsModal
