function RecusarDocumentoModal({ documento, justificativa, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Recusar documento</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          {documento.itens?.[0]?.descricaoXml || documento.numeroDocumento || 'Este documento'} —
          valor R$ {Number(documento.valorTotal ?? 0).toFixed(2)}. Recusar não pode ser desfeito.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Justificativa da recusa <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <textarea name="justificativa" value={justificativa} onChange={onChange} rows={3} required autoFocus />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Voltar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Recusando...' : 'Confirmar recusa'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default RecusarDocumentoModal
