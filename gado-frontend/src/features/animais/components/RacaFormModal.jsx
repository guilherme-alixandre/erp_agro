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

function RacaFormModal({ mode, formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar raça' : 'Editar raça'
  const submitText = isSaving ? 'Salvando...' : isCreate ? 'Cadastrar' : 'Salvar alterações'

  function handleSiglaChange(event) {
    const value = event.target.value.toUpperCase().replace(/[^A-Z]/g, '')
    onChange({ target: { name: 'sigla', value } })
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
            <RequiredLabel>Nome</RequiredLabel>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              placeholder="Ex.: Nelore, Angus"
              required
            />
          </label>

          <label>
            <RequiredLabel>Sigla (2-4 letras)</RequiredLabel>
            <input
              type="text"
              name="sigla"
              value={formData.sigla}
              onChange={handleSiglaChange}
              placeholder="Ex.: NE"
              maxLength={4}
              pattern="[A-Z]{2,4}"
              required
            />
          </label>
          <p className="form-help">
            Usada para gerar o código do brinco dos animais desta raça (ex.: NE0001, NE0002...).
            Também cria automaticamente o produto &quot;Gado {formData.nome || '...'}&quot; no catálogo de Insumos.
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

export default RacaFormModal
