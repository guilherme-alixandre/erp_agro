function formatDate(value) {
  if (!value) return '-'
  if (typeof value === 'string' && value.includes('-')) {
    const parts = value.split('-')
    if (parts.length === 3) {
      const [year, month, day] = parts
      return `${day}/${month}/${year}`
    }
  }
  return String(value)
}

function LoteDetailsModal({
  lote,
  onClose,
  onEdit,
  onDelete,
  isDeleting,
  canTransfer,
  onTransferirAnimal,
}) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>Detalhes do lote</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <dl className="details-grid">
          <div>
            <dt>Código</dt>
            <dd>{lote.codigo}</dd>
          </div>
          <div>
            <dt>Status</dt>
            <dd>{lote.statusLote === 'ATIVO' ? 'Ativo' : 'Inativo'}</dd>
          </div>
          <div>
            <dt>Cor do brinco</dt>
            <dd>{lote.corBrinco || '-'}</dd>
          </div>
          <div>
            <dt>Raça predominante</dt>
            <dd>{lote.racaPredominante || '-'}</dd>
          </div>
          <div>
            <dt>Descrição</dt>
            <dd>{lote.descricao || '-'}</dd>
          </div>
          <div>
            <dt>Data de criação</dt>
            <dd>{formatDate(lote.dataCriacao)}</dd>
          </div>
          <div>
            <dt>Total de animais</dt>
            <dd>{lote.totalAnimais}</dd>
          </div>
          <div>
            <dt>Criado por</dt>
            <dd>{lote.criadoPorNome || lote.criadoPorEmail || '-'}</dd>
          </div>
          {lote.alteradoPorNome || lote.alteradoPorEmail ? (
            <div>
              <dt>Alterado por</dt>
              <dd>{lote.alteradoPorNome || lote.alteradoPorEmail}</dd>
            </div>
          ) : null}
        </dl>

        <h3 className="details-section">Divisão por setores</h3>

        {lote.alocacoes.length === 0 ? (
          <p className="vacinas-empty">Nenhum setor alocado.</p>
        ) : (
          <div className="lote-alocacoes">
            {lote.alocacoes.map((aloc) => (
              <div
                key={aloc.loteSectorId ?? aloc.setorId}
                className="lote-alocacao"
              >
                <div className="lote-alocacao__header">
                  <strong>{aloc.setorNome}</strong>
                  <span className="lote-alocacao__capacidade">
                    {aloc.animais.length}/{aloc.capacidadeMaxima} animais
                  </span>
                </div>

                {aloc.animais.length === 0 ? (
                  <p className="vacinas-empty">
                    Nenhum animal alocado neste setor.
                  </p>
                ) : (
                  <ul className="lote-alocacao__animais">
                    {aloc.animais.map((a) => (
                      <li key={a.id} className="lote-alocacao__animal-row">
                        <span>
                          <strong>{a.codigoBrinco}</strong>
                          {a.nome ? <span> — {a.nome}</span> : null}
                        </span>
                        {canTransfer ? (
                          <button
                            type="button"
                            className="btn-row btn-row--transfer"
                            onClick={() =>
                              onTransferirAnimal(a, {
                                id: lote.id,
                                codigo: lote.codigo,
                              }, {
                                id: aloc.setorId,
                                nome: aloc.setorNome,
                              })
                            }
                          >
                            Transferir
                          </button>
                        ) : null}
                      </li>
                    ))}
                  </ul>
                )}
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
            onClick={() => onDelete(lote)}
            disabled={isDeleting}
          >
            {isDeleting ? 'Excluindo...' : 'Excluir'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default LoteDetailsModal
