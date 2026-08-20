function CancelarConsumoEstoqueModal({ consumo, justificativa, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Cancelar movimentação</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          Esta ação irá estornar ao estoque todos os produtos consumidos nesta movimentação
          registrada em {consumo.dataConsumo ? new Date(consumo.dataConsumo).toLocaleString('pt-BR') : '—'}.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Justificativa do cancelamento{' '}
              <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <textarea
              name="justificativa"
              value={justificativa}
              onChange={onChange}
              rows={3}
              required
              autoFocus
            />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Voltar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Cancelando...' : 'Confirmar cancelamento'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default CancelarConsumoEstoqueModal
