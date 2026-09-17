function RegistrarConsumoInsumoModal({
  formData,
  setores,
  insumosEstoque,
  insumoSelecionado,
  maxDataConsumo,
  isSaving,
  feedback,
  onClose,
  onChange,
  onSubmit,
}) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Registrar consumo</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="perfil-subtitle">
          Registra o consumo de um insumo em um Setor, dá baixa automática no estoque e
          calcula o rateio entre os animais alocados naquele setor.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Setor <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="setorId" value={formData.setorId} onChange={onChange} required>
              <option value="">Selecione o setor...</option>
              {setores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nome}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Insumo <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="insumoId" value={formData.insumoId} onChange={onChange} required>
              <option value="">Selecione o insumo...</option>
              {insumosEstoque.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.nome} (saldo: {i.saldoAtual} {i.unidadeMedidaPrimariaSigla})
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Quantidade consumida <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="number"
              name="quantidade"
              value={formData.quantidade}
              onChange={onChange}
              min="0.01"
              step="0.01"
              required
            />
          </label>

          {insumoSelecionado?.unidadeMedidaSecundariaId ? (
            <label>
              <span>Unidade informada</span>
              <select name="unidadeMedidaId" value={formData.unidadeMedidaId} onChange={onChange}>
                <option value={insumoSelecionado.unidadeMedidaPrimariaId}>
                  {insumoSelecionado.unidadeMedidaPrimariaSigla} (unidade de estoque)
                </option>
                <option value={insumoSelecionado.unidadeMedidaSecundariaId}>
                  {insumoSelecionado.unidadeMedidaSecundariaSigla}
                </option>
              </select>
            </label>
          ) : insumoSelecionado ? (
            <p className="form-help">Unidade: {insumoSelecionado.unidadeMedidaPrimariaSigla}</p>
          ) : null}

          <label>
            <span>Data do consumo</span>
            <input
              type="datetime-local"
              name="dataConsumo"
              value={formData.dataConsumo}
              max={maxDataConsumo}
              onChange={onChange}
            />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Registrando...' : 'Registrar consumo'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default RegistrarConsumoInsumoModal
