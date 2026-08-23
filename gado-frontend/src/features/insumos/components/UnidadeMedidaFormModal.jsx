function RequiredLabel({ children }) {
  return (
    <span>
      {children}{' '}
      <span className="required-marker" aria-hidden="true">
        *
      </span>
    </span>
  )
}

function UnidadeMedidaFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  onClose,
  onChange,
  onSubmit,
}) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar unidade de medida' : 'Editar unidade de medida'
  const submitText = isSaving ? 'Salvando...' : isCreate ? 'Cadastrar' : 'Salvar alterações'

  function handleUnidadeChange(event) {
    onChange({
      target: { name: 'unidade', value: event.target.value.toUpperCase() },
    })
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="setor-form" onSubmit={onSubmit}>
          <label>
            <RequiredLabel>Unidade</RequiredLabel>
            <input
              type="text"
              name="unidade"
              value={formData.unidade}
              onChange={handleUnidadeChange}
              placeholder="Ex.: KG, SACA, DOSE, LITRO, CABECA"
              maxLength={20}
              required
            />
          </label>
          <p className="form-help">
            Usada como unidade de estoque (primária ou secundária) dos produtos do catálogo de
            Insumos. Renomear uma unidade já em uso não altera os produtos já cadastrados.
          </p>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
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

export default UnidadeMedidaFormModal
