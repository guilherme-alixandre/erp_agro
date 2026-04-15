const STATUS_OPTIONS = ['EX1', 'EX2']

function AnimalFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  onClose,
  onChange,
  onSubmit,
}) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar animal' : 'Editar animal'
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
            <span>Código do brinco</span>
            <input
              type="text"
              name="codigoBrinco"
              value={formData.codigoBrinco}
              onChange={onChange}
              required
              disabled={!isCreate}
            />
          </label>

          <label>
            <span>Nome</span>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Data de nascimento</span>
            <input
              type="date"
              name="dataNascimento"
              value={formData.dataNascimento}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Peso atual</span>
            <input
              type="number"
              min="0"
              step="0.01"
              name="pesoAtual"
              value={formData.pesoAtual}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Raça</span>
            <input
              type="text"
              name="raca"
              value={formData.raca}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Cor</span>
            <input
              type="text"
              name="cor"
              value={formData.cor}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Tamanho</span>
            <input
              type="text"
              name="tamanho"
              value={formData.tamanho}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>Sexo</span>
            <select name="sexo" value={formData.sexo} onChange={onChange}>
              <option value="M">Macho</option>
              <option value="F">Fêmea</option>
            </select>
          </label>

          <label>
            <span>Status</span>
            <select
              name="statusAnimal"
              value={formData.statusAnimal}
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

export default AnimalFormModal
