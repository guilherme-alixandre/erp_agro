function LoteFormModal({
  mode,
  formData,
  isSaving,
  feedback,
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
          <label>
            <span>ID do Usuário Responsável</span>
            <input
              type="number"
              name="usuario_id"
              value={formData.usuario_id}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Descrição do Lote</span>
            <input
              type="text"
              name="descricao"
              value={formData.descricao}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Raça Predominante</span>
            <input
              type="text"
              name="racaPredominante"
              value={formData.racaPredominante}
              onChange={onChange}
              required
            />
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
