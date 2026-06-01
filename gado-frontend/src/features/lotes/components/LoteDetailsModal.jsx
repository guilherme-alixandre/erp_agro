function LoteDetailsModal({ lote, onClose, onEdit, onDelete, isDeleting }) {
  return (
      <div className="modal-overlay" role="dialog" aria-modal="true">
        <div className="modal-card">
          <div className="modal-header">
            <h2>Detalhes do lote</h2>
            <button type="button" className="modal-close" onClick={onClose}>
              ✕
            </button>
          </div>

          <dl className="details-grid">
            <div>
              <dt>ID</dt>
              <dd>{lote.id}</dd>
            </div>
            <div>
              <dt>Descrição</dt>
              <dd>{lote.descricao || '-'}</dd>
            </div>
            <div>
              <dt>Raça Predominante</dt>
              <dd>{lote.racaPredominante || '-'}</dd>
            </div>
            <div>
              <dt>Usuário</dt>
              <dd>{lote.usuario?.nome || '-'}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{lote.status || '-'}</dd>
            </div>
            <div>
              <dt>Criado em</dt>
              <dd>{lote.created_at ? new Date(lote.created_at).toLocaleDateString() : '-'}</dd>
            </div>
          </dl>

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