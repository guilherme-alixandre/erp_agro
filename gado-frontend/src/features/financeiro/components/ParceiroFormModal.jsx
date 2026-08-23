const TIPOS = [
  { value: 'FORNECEDOR', label: 'Fornecedor' },
  { value: 'COMPRADOR', label: 'Comprador' },
  { value: 'AMBOS', label: 'Ambos' },
]

function ParceiroFormModal({ mode = 'create', formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  const isEdit = mode === 'edit'

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>{isEdit ? 'Editar parceiro' : 'Cadastrar parceiro'}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={onSubmit}>
          <div className="financeiro-edit-grid">
            <label>
              <span>
                Nome <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input type="text" name="nome" value={formData.nome} onChange={onChange} required />
            </label>

            <label>
              <span>
                CPF/CNPJ <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input
                type="text"
                name="cpfCnpj"
                value={formData.cpfCnpj}
                onChange={onChange}
                placeholder="000.000.000-00 ou 00.000.000/0000-00"
                required
                disabled={isEdit}
              />
            </label>

            <label>
              <span>E-mail</span>
              <input type="email" name="email" value={formData.email} onChange={onChange} />
            </label>

            <label>
              <span>Telefone</span>
              <input type="text" name="telefone" value={formData.telefone} onChange={onChange} />
            </label>

            <label>
              <span>Endereço</span>
              <input type="text" name="endereco" value={formData.endereco} onChange={onChange} />
            </label>

            <label>
              <span>
                Tipo <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <select name="tipo" value={formData.tipo} onChange={onChange} required>
                <option value="" disabled>Selecione...</option>
                {TIPOS.map((t) => (
                  <option key={t.value} value={t.value}>{t.label}</option>
                ))}
              </select>
            </label>
          </div>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ParceiroFormModal
