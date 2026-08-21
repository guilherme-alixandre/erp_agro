function FuncionarioFormModal({ formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>Cadastrar funcionário</h2>
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
              <input type="text" name="cpf" value={formData.cpf} onChange={onChange} placeholder="000.000.000-00" required />
            </label>

            <label>
              <span>
                Cargo <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input type="text" name="cargo" value={formData.cargo} onChange={onChange} required />
            </label>

            <label>
              <span>
                Data de admissão <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input type="date" name="dataAdmissao" value={formData.dataAdmissao} onChange={onChange} required />
            </label>

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

            <label>
              <span>
                Natureza financeira <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <select name="naturezaFinanceira" value={formData.naturezaFinanceira} onChange={onChange} required>
                <option value="" disabled>Selecione...</option>
                <option value="CUSTO">Custo (ligado à produção, ex.: cuidador)</option>
                <option value="GASTO">Gasto (administrativo, ex.: financeiro)</option>
              </select>
            </label>
          </div>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Cadastrar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default FuncionarioFormModal
