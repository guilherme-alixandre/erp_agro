const STATUS_OPTIONS = ['ATIVO', 'INATIVO', 'ENCERRADO']

function RequiredLabel({ children }) {
  return (
    <span>
      {children} <span className="required-marker" aria-hidden="true">*</span>
    </span>
  )
}

function LoteFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  userEmail,
  onClose,
  onChange,
  onSubmit,
}) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar lote' : 'Editar lote'
  const submitText = isSaving
    ? 'Salvando...'
    : isCreate
      ? 'Cadastrar'
      : 'Salvar alterações'

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="lote-form" onSubmit={onSubmit}>

          {isCreate && userEmail ? (
            <p className="form-info">
              Cadastrando como <strong>{userEmail}</strong>
            </p>
          ) : null}

          <label>
            <RequiredLabel>Descrição</RequiredLabel>
            <input
              type="text"
              name="descricao"
              value={formData.descricao}
              onChange={onChange}
              placeholder="Ex.: Lote de Engorda - Pasto 01"
              required
            />
          </label>

          <label>
            <RequiredLabel>Raça Predominante</RequiredLabel>
            <input
              type="text"
              name="racaPredominante"
              value={formData.racaPredominante}
              onChange={onChange}
              placeholder="Ex.: Nelore"
              required
            />
          </label>

          <label>
            <span>Status</span>
            <select
              name="status"
              value={formData.status}
              onChange={onChange}
            >
              {STATUS_OPTIONS.map((status) => (
                <option key={status} value={status}>
                  {status}
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
              {submitText}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default LoteFormModal
