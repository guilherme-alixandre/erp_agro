const CARGOS = [
  { value: 'GERENTE', label: 'Gerente' },
  { value: 'CUIDADOR', label: 'Cuidador' },
  { value: 'CUIDADOR_CHEFE', label: 'Cuidador Chefe' },
  { value: 'ADMINISTRADOR', label: 'Administrador' },
  { value: 'FINANCEIRO', label: 'Financeiro' },
]

function FuncionarioFormModal({ mode = 'create', formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  const isEdit = mode === 'edit'

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>{isEdit ? 'Editar funcionário' : 'Cadastrar funcionário'}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={onSubmit}>
          <div className="financeiro-edit-grid">
            <label>
              <span>
                Nome completo <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input type="text" name="nomeCompleto" value={formData.nomeCompleto} onChange={onChange} required />
            </label>

            <label>
              <span>
                CPF <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input
                type="text"
                name="cpf"
                value={formData.cpf}
                onChange={onChange}
                placeholder="000.000.000-00"
                required
                disabled={isEdit}
              />
            </label>

            <label>
              <span>
                Cargo <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <select name="cargo" value={formData.cargo} onChange={onChange} required>
                <option value="" disabled>Selecione...</option>
                {CARGOS.map((c) => (
                  <option key={c.value} value={c.value}>{c.label}</option>
                ))}
              </select>
            </label>

            <label>
              <span>
                Data de admissão <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input
                type="date"
                name="dataAdmissao"
                value={formData.dataAdmissao}
                onChange={onChange}
                required
                disabled={isEdit}
              />
            </label>

            {isEdit ? (
              <label>
                <span>Data de demissão</span>
                <input type="date" name="dataDemissao" value={formData.dataDemissao ?? ''} onChange={onChange} />
              </label>
            ) : null}

            <label>
              <span>
                Salário base (R$) <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input type="number" name="salarioBase" value={formData.salarioBase} onChange={onChange} min="0" step="0.01" required />
            </label>

            <label>
              <span>
                INSS (%) <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input type="number" name="percentualInss" value={formData.percentualInss} onChange={onChange} min="0" max="100" step="0.01" required />
            </label>

            <label>
              <span>
                FGTS (%) <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input type="number" name="percentualFgts" value={formData.percentualFgts} onChange={onChange} min="0" max="100" step="0.01" required />
            </label>

            <label>
              <span>Vale transporte (R$)</span>
              <input type="number" name="valorValeTransporte" value={formData.valorValeTransporte} onChange={onChange} min="0" step="0.01" />
            </label>

            <label>
              <span>Vale alimentação (R$)</span>
              <input type="number" name="valorValeAlimentacao" value={formData.valorValeAlimentacao} onChange={onChange} min="0" step="0.01" />
            </label>

            <label>
              <span>Plano de saúde (R$)</span>
              <input type="number" name="valorPlanoSaude" value={formData.valorPlanoSaude} onChange={onChange} min="0" step="0.01" />
            </label>
          </div>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Salvando...' : isEdit ? 'Salvar alterações' : 'Cadastrar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default FuncionarioFormModal
