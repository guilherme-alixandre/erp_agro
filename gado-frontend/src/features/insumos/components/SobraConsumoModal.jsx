const STATUS_LABELS = {
  OK: 'Dentro da faixa ideal',
  ABAIXO: 'Sobra abaixo do ideal',
  ACIMA: 'Sobra acima do ideal',
  PERDA: 'Registrada como perda',
}

function SobraConsumoModal({
  consumo,
  insumoSelecionado,
  formData,
  isSaving,
  feedback,
  resultado,
  onClose,
  onChange,
  onSubmit,
}) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Sobra — {consumo.insumoNome}</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-help">
          Quantidade aplicada nesta alimentação: {consumo.quantidadeBaixaUnidadePrimaria}{' '}
          {consumo.unidadeMedidaPrimariaSigla}.
        </p>

        {resultado ? (
          <>
            <p
              className={`feedback ${
                resultado.statusFaixa === 'PERDA' || resultado.statusFaixa === 'ACIMA'
                  ? 'feedback--error'
                  : 'feedback--info'
              }`}
            >
              {STATUS_LABELS[resultado.statusFaixa] ?? ''}: {resultado.mensagem}
            </p>
            <div className="modal-actions">
              <button type="button" className="btn-primary" onClick={onClose}>
                Fechar
              </button>
            </div>
          </>
        ) : (
          <form className="animal-form" onSubmit={onSubmit}>
            <label>
              <span>
                Quantidade de sobra <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input
                type="number"
                name="quantidade"
                value={formData.quantidade}
                onChange={onChange}
                min="0"
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
              <span>
                Foi reaproveitada? <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <select name="reaproveitado" value={formData.reaproveitado} onChange={onChange} required>
                <option value="true">Sim</option>
                <option value="false">Não (perda)</option>
              </select>
            </label>

            {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary" disabled={isSaving}>
                {isSaving ? 'Salvando...' : 'Salvar sobra'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

export default SobraConsumoModal
