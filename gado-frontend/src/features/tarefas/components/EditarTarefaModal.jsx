function EditarTarefaModal({ formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Editar tarefa</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Descrição <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <textarea name="descricao" value={formData.descricao} onChange={onChange} rows={3} required />
          </label>

          <label>
            <span>Prazo</span>
            <input type="date" name="dataLimite" value={formData.dataLimite} onChange={onChange} />
          </label>

          <label className="checkbox-field">
            <input
              type="checkbox"
              name="statusConclusao"
              checked={formData.statusConclusao}
              onChange={onChange}
            />
            <span>Concluída</span>
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
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

export default EditarTarefaModal
