function AtribuirTarefaModal({ formData, usuarios, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Atribuir tarefa</h2>
          <button type="button" className="modal-close" onClick={onClose}>
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

          <label>
            <span>
              Atribuir para <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="atribuidoParaEmail" value={formData.atribuidoParaEmail} onChange={onChange} required>
              <option value="" disabled>Selecione...</option>
              {usuarios.map((u) => (
                <option key={u.email} value={u.email}>
                  {u.nome} ({u.email})
                </option>
              ))}
            </select>
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Atribuir'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AtribuirTarefaModal
