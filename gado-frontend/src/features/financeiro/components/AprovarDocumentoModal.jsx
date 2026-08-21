function AprovarDocumentoModal({ documento, isSaving, feedback, onClose, onConfirm }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Aprovar documento</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          {documento.itens?.[0]?.descricaoXml || documento.numeroDocumento || 'Este documento'} —
          valor R$ {Number(documento.valorTotal ?? 0).toFixed(2)}. Ao aprovar, ele passa a compor
          o resumo financeiro do mês.
        </p>

        {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
            Cancelar
          </button>
          <button type="button" className="btn-primary" onClick={onConfirm} disabled={isSaving}>
            {isSaving ? 'Aprovando...' : 'Confirmar aprovação'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default AprovarDocumentoModal
