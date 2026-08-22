function EstornarPagamentoModal({ pagamento, motivoEstorno, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Extornar pagamento</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          Esta ação irá extornar o pagamento de <strong>{pagamento.funcionarioNome}</strong> referente a{' '}
          {String(pagamento.mesReferencia).padStart(2, '0')}/{pagamento.anoReferencia}, removendo-o do
          lançamento financeiro do mês.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Motivo do estorno <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <textarea name="motivoEstorno" value={motivoEstorno} onChange={onChange} rows={3} required autoFocus />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Voltar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Extornando...' : 'Confirmar extorno'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EstornarPagamentoModal
