function EntradaEstoqueModal({ insumo, formData, isSaving, feedback, onClose, onChange, onSubmit }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Registrar entrada de estoque</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          <strong>{insumo.nome}</strong> — saldo atual: {insumo.saldoAtual} {insumo.unidadeMedidaPrimariaSigla}
          {insumo.precoCompraMedio != null ? ` · preço médio atual: R$ ${insumo.precoCompraMedio.toFixed(2)}` : ''}
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Quantidade ({insumo.unidadeMedidaPrimariaSigla || 'unidade primária'}){' '}
              <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="number"
              name="quantidade"
              value={formData.quantidade}
              onChange={onChange}
              min="0.01"
              step="0.01"
              required
              autoFocus
            />
          </label>

          <label>
            <span>
              Preço unitário de compra (R$/{insumo.unidadeMedidaPrimariaSigla || 'unidade'}){' '}
              <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="number"
              name="precoUnitario"
              value={formData.precoUnitario}
              onChange={onChange}
              min="0.01"
              step="0.01"
              required
            />
          </label>

          <label>
            <span>Data da entrada</span>
            <input
              type="datetime-local"
              name="dataEntrada"
              value={formData.dataEntrada}
              onChange={onChange}
            />
          </label>

          <fieldset className="senha-fieldset">
            <legend>Nota fiscal (opcional)</legend>
            <p className="form-help">
              Base para a futura importação automática de XML de NF-e.
            </p>

            <label>
              <span>Número da NF</span>
              <input
                type="text"
                name="numeroNf"
                value={formData.numeroNf}
                onChange={onChange}
              />
            </label>

            <label>
              <span>Chave de acesso</span>
              <input
                type="text"
                name="chaveAcessoNf"
                value={formData.chaveAcessoNf}
                onChange={onChange}
                maxLength={44}
                placeholder="44 dígitos"
              />
            </label>
          </fieldset>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Registrando...' : 'Registrar entrada'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EntradaEstoqueModal
