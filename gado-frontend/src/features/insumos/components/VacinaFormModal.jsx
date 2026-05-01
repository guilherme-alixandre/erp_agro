function VacinaFormModal({
  mode,
  formData,
  isSaving,
  isDeleting,
  feedback,
  onClose,
  onChange,
  onSubmit,
  onConfirmar,
  onDelete,
}) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar vacina' : 'Editar vacina'
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

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Nome <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              placeholder="Ex.: Aftosa"
              required
            />
          </label>

          {!isCreate && formData.pendente ? (
            <p className="feedback feedback--info">
              Esta vacina foi cadastrada automaticamente e está como{' '}
              <strong>pendente</strong>. Confirme para concluir o cadastro.
            </p>
          ) : null}

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions vacina-modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>

            {!isCreate ? (
              <>
                {formData.pendente ? (
                  <button
                    type="button"
                    className="btn-primary"
                    onClick={onConfirmar}
                    disabled={isSaving}
                  >
                    Confirmar vacina
                  </button>
                ) : null}
                <button
                  type="button"
                  className="btn-danger"
                  onClick={onDelete}
                  disabled={isDeleting}
                >
                  {isDeleting ? 'Excluindo...' : 'Excluir'}
                </button>
              </>
            ) : null}

            <button type="submit" className="btn-primary" disabled={isSaving}>
              {submitText}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default VacinaFormModal
