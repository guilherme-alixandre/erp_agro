const TIPO_SETOR_OPTIONS = ['TIPO1', 'TIPO2']

function RequiredLabel({ children }) {
  return (
    <span>
      {children} <span className="required-marker" aria-hidden="true">*</span>
    </span>
  )
}

function SetorFormModal({
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
  const title = isCreate ? 'Cadastrar setor' : 'Editar setor'
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

        <form className="setor-form" onSubmit={onSubmit}>

          {isCreate && userEmail ? (
            <p className="form-info">
              Cadastrando como <strong>{userEmail}</strong>
            </p>
          ) : null}

          <label>
            <RequiredLabel>Nome do Setor</RequiredLabel>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              placeholder="Ex.: Pasto 01"
              required
            />
          </label>

          <label>
            <RequiredLabel>Tipo de Setor</RequiredLabel>
            <select
              name="setor"
              value={formData.setor}
              onChange={onChange}
              required
            >
              {TIPO_SETOR_OPTIONS.map((tipo) => (
                <option key={tipo} value={tipo}>
                  {tipo}
                </option>
              ))}
            </select>
          </label>

          <label>
            <RequiredLabel>Capacidade Máxima (animais)</RequiredLabel>
            <input
              type="number"
              name="capacidadeMaxima"
              value={formData.capacidadeMaxima}
              onChange={onChange}
              placeholder="Ex.: 100"
              required
            />
          </label>

          <label>
            <RequiredLabel>Meta de Texto</RequiredLabel>
            <input
              type="text"
              name="metaTexto"
              value={formData.metaTexto}
              onChange={onChange}
              placeholder="Ex.: Engorda"
              required
            />
          </label>

          <label>
            <span>Meta de Produção de Leite (L)</span>
            <input
              type="number"
              name="metaProducaoLeite"
              value={formData.metaProducaoLeite}
              onChange={onChange}
              placeholder="Ex.: 500"
              step="0.01"
            />
          </label>

          <label>
            <span>Meta de Arroba para Abate (@)</span>
            <input
              type="number"
              name="metaArrobaAbate"
              value={formData.metaArrobaAbate}
              onChange={onChange}
              placeholder="Ex.: 25"
              step="0.01"
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

export default SetorFormModal
