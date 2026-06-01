function SetorDetailsModal({ setor, onClose, onEdit, onDelete, isDeleting }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Detalhes do setor</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <dl className="details-grid">
          <div>
            <dt>ID</dt>
            <dd>{setor.id}</dd>
          </div>
          <div>
            <dt>Nome</dt>
            <dd>{setor.nome || '-'}</dd>
          </div>
          <div>
            <dt>Tipo de Setor</dt>
            <dd>{setor.setor || '-'}</dd>
          </div>
          <div>
            <dt>Capacidade Máxima</dt>
            <dd>{setor.capacidadeMaxima || '-'} animais</dd>
          </div>
          <div>
            <dt>Meta de Texto</dt>
            <dd>{setor.metaTexto || '-'}</dd>
          </div>
          <div>
            <dt>Meta de Produção de Leite</dt>
            <dd>{setor.metaProducaoLeite ? `${setor.metaProducaoLeite} L` : '-'}</dd>
          </div>
          <div>
            <dt>Meta de Arroba para Abate</dt>
            <dd>{setor.metaArrobaAbate ? `${setor.metaArrobaAbate} @` : '-'}</dd>
          </div>
        </dl>

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
