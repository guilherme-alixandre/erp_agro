function RegistrarVacinacaoModal({
  formData,
  vacinasDisponiveis,
  insumoSelecionado,
  lotes,
  maxDataAplicacao,
  isSaving,
  feedback,
  onClose,
  onChange,
  onLoteSelecionado,
  onAbrirSelecaoAnimais,
  onSubmit,
}) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Registrar aplicação</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="perfil-subtitle">
          Registra a aplicação de um produto (vacina) em um ou mais animais, ou em um lote
          inteiro, com baixa automática no estoque (dose x nº de animais).
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Vacina <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="insumoId" value={formData.insumoId} onChange={onChange} required>
              <option value="">Selecione o produto...</option>
              {vacinasDisponiveis.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.nome} (saldo: {i.saldoAtual} {i.unidadeMedidaPrimariaSigla})
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Dose por animal <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="number"
              name="quantidadePorAnimal"
              value={formData.quantidadePorAnimal}
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
            <span>Seleção</span>
            <select name="modoSelecao" value={formData.modoSelecao} onChange={onChange}>
              <option value="ANIMAIS">Animais individuais</option>
              <option value="LOTE">Um lote inteiro</option>
            </select>
          </label>

          {formData.modoSelecao === 'LOTE' ? (
            <label>
              <span>Lote <span className="required-marker" aria-hidden="true">*</span></span>
              <select value={formData.loteId} onChange={onLoteSelecionado} required>
                <option value="">Selecione o lote...</option>
                {lotes.map((l) => (
                  <option key={l.id} value={l.id}>
                    {l.codigo}{l.descricao ? ` — ${l.descricao}` : ''}
                  </option>
                ))}
              </select>
            </label>
          ) : null}

          <div className="setores-fieldset__select">
            <span>Animais selecionados</span>
            <button type="button" className="ssm-trigger" onClick={onAbrirSelecaoAnimais}>
              <span className={formData.animalIds.length ? '' : 'ssm-trigger__placeholder'}>
                {formData.animalIds.length > 0
                  ? `${formData.animalIds.length} animal(is) selecionado(s)`
                  : 'Selecionar animais...'}
              </span>
              <span className="ssm-trigger__arrow" aria-hidden="true">▼</span>
            </button>
          </div>

          <label>
            <span>Data da aplicação</span>
            <input
              type="datetime-local"
              name="dataAplicacao"
              value={formData.dataAplicacao}
              max={maxDataAplicacao}
              onChange={onChange}
            />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving || formData.animalIds.length === 0}>
              {isSaving ? 'Registrando...' : 'Registrar aplicação'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default RegistrarVacinacaoModal
