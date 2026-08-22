const MESES = [
  '01 - Janeiro', '02 - Fevereiro', '03 - Março', '04 - Abril', '05 - Maio', '06 - Junho',
  '07 - Julho', '08 - Agosto', '09 - Setembro', '10 - Outubro', '11 - Novembro', '12 - Dezembro',
]

function LancarPagamentoModal({ funcionario, formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Lançar pagamento — {funcionario.nomeCompleto}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          Salário base R$ {Number(funcionario.salarioBase ?? 0).toFixed(2)}. O bruto, o desconto de
          INSS, o encargo de FGTS e o líquido são calculados automaticamente a partir do cadastro
          do funcionário.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Ano de referência <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input type="number" name="anoReferencia" value={formData.anoReferencia} onChange={onChange} min="2000" required />
          </label>

          <label>
            <span>
              Mês de referência <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="mesReferencia" value={formData.mesReferencia} onChange={onChange} required>
              <option value="" disabled>Selecione...</option>
              {MESES.map((label, index) => (
                <option key={label} value={index + 1}>{label}</option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Data de pagamento <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input type="date" name="dataPagamento" value={formData.dataPagamento} onChange={onChange} required />
          </label>

          <label>
            <span>Descontos extras do mês (R$)</span>
            <input type="number" name="descontoOutros" value={formData.descontoOutros} onChange={onChange} min="0" step="0.01" />
          </label>

          <label>
            <span>Bônus do mês (R$)</span>
            <input type="number" name="valorBonus" value={formData.valorBonus} onChange={onChange} min="0" step="0.01" />
          </label>

          <p className="form-help">Descontos já calculados automaticamente.</p>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Lançando...' : 'Lançar pagamento'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default LancarPagamentoModal
