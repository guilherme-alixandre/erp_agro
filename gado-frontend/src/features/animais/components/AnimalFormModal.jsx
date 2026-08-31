const STATUS_OPTIONS = ['ABATIDO', 'OBITO', 'ATIVO', 'OBSERVACAO', 'VENDIDO']
const STATUS_LABELS = {
  ABATIDO: 'Abatido',
  OBITO: 'Óbito',
  ATIVO: 'Ativo',
  OBSERVACAO: 'Em observação',
  VENDIDO: 'Vendido',
}

const MIN_BIRTH_DATE = '1990-01-01'

function todayIso() {
  const now = new Date()
  const yyyy = now.getFullYear()
  const mm = String(now.getMonth() + 1).padStart(2, '0')
  const dd = String(now.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function RequiredLabel({ children }) {
  return (
    <span>
      {children} <span className="required-marker" aria-hidden="true">*</span>
    </span>
  )
}

function AnimalFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  userEmail,
  racasDisponiveis,
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
  const maxBirthDate = todayIso()

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

          {isCreate && userEmail ? (
            <p className="form-info">
              Cadastrando como <strong>{userEmail}</strong>
            </p>
          ) : null}

          {!isCreate ? (
            <label>
              <span>Código do brinco</span>
              <input
                type="text"
                name="codigoBrinco"
                value={formData.codigoBrinco}
                disabled
              />
            </label>
          ) : (
            <p className="form-help">
              O código do brinco é gerado automaticamente a partir da raça escolhida.
            </p>
          )}

          <label>
            <RequiredLabel>Data de nascimento</RequiredLabel>
            <input
              type="date"
              name="dataNascimento"
              value={formData.dataNascimento}
              onChange={onChange}
              min={MIN_BIRTH_DATE}
              max={maxBirthDate}
              required
            />
          </label>

          <label>
            <RequiredLabel>Peso atual (kg)</RequiredLabel>
            <input
              type="number"
              min="30"
              max="1500"
              step="0.01"
              name="pesoAtual"
              value={formData.pesoAtual}
              onChange={onChange}
              placeholder="Ex.: 320.5"
              required
            />
          </label>

          <label>
            <RequiredLabel>Raça</RequiredLabel>
            <select name="racaId" value={formData.racaId} onChange={onChange} required>
              <option value="" disabled>Selecione a raça...</option>
              {(racasDisponiveis ?? []).map((raca) => (
                <option key={raca.id} value={raca.id}>{raca.nome}</option>
              ))}
            </select>
          </label>

          <label>
            <RequiredLabel>Cor</RequiredLabel>
            <input
              type="text"
              name="cor"
              value={formData.cor}
              onChange={onChange}
              required
            />
          </label>

          <p className="form-help">
            Medidas zootécnicas — preencha as que você medir.
          </p>

          <label>
            <span>Altura na cernelha (cm)</span>
            <input
              type="number"
              min="0"
              max="350"
              step="0.1"
              name="alturaCernelha"
              value={formData.alturaCernelha}
              onChange={onChange}
              placeholder="cm"
            />
          </label>

          <label>
            <span>Perímetro torácico (cm)</span>
            <input
              type="number"
              min="0"
              max="350"
              step="0.1"
              name="perimetroToracico"
              value={formData.perimetroToracico}
              onChange={onChange}
              placeholder="cm"
            />
          </label>

          <label>
            <span>Comprimento corporal (cm)</span>
            <input
              type="number"
              min="0"
              max="350"
              step="0.1"
              name="comprimentoCorporal"
              value={formData.comprimentoCorporal}
              onChange={onChange}
              placeholder="cm"
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
                  {STATUS_LABELS[status]}
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
