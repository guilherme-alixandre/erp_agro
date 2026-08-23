function EditarVacinacaoAnimalModal({ vacinacao, formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Editar aplicação — {vacinacao.insumoNome}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-help">
          Cobre {vacinacao.totalAnimais} {vacinacao.totalAnimais === 1 ? 'animal' : 'animais'}. A edição não
          altera quais animais foram cobertos, apenas a dose e a data.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>Dose por animal</span>
            <input
              type="number"
              name="quantidadePorAnimal"
              value={formData.quantidadePorAnimal}
              onChange={onChange}
              min="0.01"
              step="0.01"
              required
            />
          </label>

          <label>
            <span>Data da aplicação</span>
            <input
              type="datetime-local"
              name="dataAplicacao"
              value={formData.dataAplicacao}
              max={formData.maxDataAplicacao}
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

export default EditarVacinacaoAnimalModal
