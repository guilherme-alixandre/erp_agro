function EditarConsumoEstoqueModal({
  consumo,
  formData,
  insumosEstoque,
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
          <h2>Editar movimentação — {consumo.motivo}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={onSubmit}>
          <div className="consumo-estoque__itens">
            {formData.itens.map((item, index) => {
              const insumoSelecionado = insumoPorId(item.insumoId)
              return (
                <div className="consumo-estoque__item-row" key={index}>
                  <label className="consumo-estoque__item-produto">
                    <span>Produto</span>
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
                    <span>Quantidade</span>
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
                    ✕
                  </button>
                </div>
              )
            })}

            <button type="button" className="btn-secondary consumo-estoque__add-item" onClick={onAddItem}>
              + Adicionar produto
            </button>
          </div>

          <label>
            <span>Motivo / finalidade da saída</span>
            <textarea name="motivo" value={formData.motivo} onChange={onChange} rows={2} required />
          </label>

          <label>
            <span>Data do consumo</span>
            <input
              type="datetime-local"
              name="dataConsumo"
              value={formData.dataConsumo}
              max={formData.maxDataConsumo}
              onChange={onChange}
            />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Salvar alterações'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EditarConsumoEstoqueModal
