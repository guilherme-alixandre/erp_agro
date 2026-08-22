function EditarConsumoInsumoModal({ consumo, formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Editar lançamento — {consumo.insumoNome}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>Quantidade consumida</span>
            <input
              type="number"
              name="quantidade"
              value={formData.quantidade}
              onChange={onChange}
              min="0.01"
              step="0.01"
              required
            />
          </label>

          <label>
            <span>Data do consumo</span>
            <input
              type="datetime-local"
              name="dataConsumo"
              value={formData.dataConsumo}
              max={formData.maxDataConsumo}
              onChange={onChange}
            />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Salvar alterações'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EditarConsumoInsumoModal
