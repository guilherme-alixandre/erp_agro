function RegistrarConsumoEstoqueModal({
  formData,
  insumosEstoque,
  maxDataConsumo,
  isSaving,
  feedback,
  onClose,
  onChange,
  onItemChange,
  onAddItem,
  onRemoveItem,
  onSubmit,
}) {
  function insumoPorId(insumoId) {
    return insumosEstoque.find((i) => String(i.id) === String(insumoId)) ?? null
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Registrar saída de estoque</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="perfil-subtitle">
          Registra a saída de um ou mais produtos do próprio estoque, com o motivo/finalidade
          da retirada. Dá baixa automática no saldo dos produtos selecionados.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <div className="consumo-estoque__itens">
            {formData.itens.map((item, index) => {
              const insumoSelecionado = insumoPorId(item.insumoId)
              return (
                <div className="consumo-estoque__item-row" key={index}>
                  <label className="consumo-estoque__item-produto">
                    <span>
                      Produto <span className="required-marker" aria-hidden="true">*</span>
                    </span>
                    <select name="insumoId" value={item.insumoId} onChange={(e) => onItemChange(index, e)} required>
                      <option value="">Selecione...</option>
                      {insumosEstoque.map((i) => (
                        <option key={i.id} value={i.id}>
                          {i.nome} (saldo: {i.saldoAtual} {i.unidadeMedidaPrimariaSigla})
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="consumo-estoque__item-quantidade">
                    <span>
                      Quantidade <span className="required-marker" aria-hidden="true">*</span>
                    </span>
                    <div className="consumo-estoque__quantidade-group">
                      <input
                        type="number"
                        name="quantidade"
                        value={item.quantidade}
                        onChange={(e) => onItemChange(index, e)}
                        min="0.01"
                        step="0.01"
                        required
                      />
                      {insumoSelecionado?.unidadeMedidaSecundariaId ? (
                        <select
                          name="unidadeMedidaId"
                          className="consumo-estoque__unidade-inline"
                          value={item.unidadeMedidaId}
                          onChange={(e) => onItemChange(index, e)}
                        >
                          <option value={insumoSelecionado.unidadeMedidaPrimariaId}>
                            {insumoSelecionado.unidadeMedidaPrimariaSigla}
                          </option>
                          <option value={insumoSelecionado.unidadeMedidaSecundariaId}>
                            {insumoSelecionado.unidadeMedidaSecundariaSigla}
                          </option>
                        </select>
                      ) : insumoSelecionado ? (
                        <span className="consumo-estoque__unidade-sufixo">
                          {insumoSelecionado.unidadeMedidaPrimariaSigla}
                        </span>
                      ) : null}
                    </div>
                  </label>

                  <button
                    type="button"
                    className="btn-icon btn-icon--danger consumo-estoque__remove-item"
                    onClick={() => onRemoveItem(index)}
                    disabled={formData.itens.length === 1}
                    aria-label="Remover produto"
                    title="Remover produto"
                  >
                    <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                      <path
                        fill="currentColor"
                        d="M9 3a1 1 0 0 0-1 1v1H4a1 1 0 1 0 0 2h1v13a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7h1a1 1 0 1 0 0-2h-4V4a1 1 0 0 0-1-1H9zm1 2h4v0h-4v0zM7 7h10v13H7V7zm3 2a1 1 0 0 0-1 1v7a1 1 0 1 0 2 0v-7a1 1 0 0 0-1-1zm4 0a1 1 0 0 0-1 1v7a1 1 0 1 0 2 0v-7a1 1 0 0 0-1-1z"
                      />
                    </svg>
                  </button>
                </div>
              )
            })}

            <button type="button" className="btn-secondary consumo-estoque__add-item" onClick={onAddItem}>
              + Adicionar produto
            </button>
          </div>

          <label>
            <span>
              Motivo / finalidade da saída <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <textarea name="motivo" value={formData.motivo} onChange={onChange} rows={2} required />
          </label>

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
              {isSaving ? 'Registrando...' : 'Registrar saída'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default RegistrarConsumoEstoqueModal
