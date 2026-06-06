// Ajuste os valores para corresponder exatamente ao enum EnTipoSetor do backend.
const TIPOS_SETOR = [
  { value: 'LEITE', label: 'Leite' },
  { value: 'ABATE', label: 'Abate' },
  { value: 'CRIA', label: 'Cria' },
  { value: 'RECRIA', label: 'Recria' },
]

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

function SetorFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  currentUser,
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
          {isCreate ? (
            <p className="form-info">
              Cadastrando como <strong>{currentUser.email}</strong>
            </p>
          ) : (
            <label>
              <span>ID do setor</span>
              <input type="text" value={formData.id ?? ''} readOnly disabled />
            </label>
          )}

          <label>
            <RequiredLabel>Nome</RequiredLabel>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              placeholder="Ex.: Pasto Norte, Curral 01"
              required
            />
          </label>

          <label>
            <RequiredLabel>Capacidade máxima</RequiredLabel>
            <input
              type="number"
              name="capacidadeMaxima"
              value={formData.capacidadeMaxima}
              onChange={onChange}
              placeholder="Nº máximo de animais"
              min="1"
              required
            />
          </label>

          <label>
            <RequiredLabel>Tipo</RequiredLabel>
            <select
              name="tipo"
              value={formData.tipo}
              onChange={onChange}
              required
            >
              <option value="">Selecione o tipo...</option>
              {TIPOS_SETOR.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>Observações</span>
            <textarea
              name="metaTexto"
              value={formData.metaTexto}
              onChange={onChange}
              placeholder="Observações opcionais sobre o setor"
              rows={3}
            />
          </label>

          {feedback ? (
            <p className="feedback feedback--error">{feedback}</p>
          ) : null}

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
