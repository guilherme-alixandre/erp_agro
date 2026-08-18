const TIPO_OPTIONS = ['RACAO', 'MEDICAMENTO', 'OUTROS']

const TIPO_LABELS = {
  RACAO: 'Ração',
  MEDICAMENTO: 'Medicamento',
  OUTROS: 'Outros',
}

function InsumoEstoqueFormModal({
  mode,
  formData,
  unidades,
  isSaving,
  feedback,
  onClose,
  onChange,
  onSubmit,
}) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar insumo' : 'Editar insumo'
  const submitText = isSaving ? 'Salvando...' : isCreate ? 'Cadastrar' : 'Salvar alterações'
  const temUnidadeSecundaria = Boolean(formData.unidadeMedidaSecundariaId)

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
          <label>
            <span>
              Nome <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              placeholder="Ex.: Ração Engorda Premium"
              required
            />
          </label>

          {isCreate ? (
            <label>
              <span>
                Tipo <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <select name="tipo" value={formData.tipo} onChange={onChange}>
                {TIPO_OPTIONS.map((tipo) => (
                  <option key={tipo} value={tipo}>
                    {TIPO_LABELS[tipo] ?? tipo}
                  </option>
                ))}
              </select>
            </label>
          ) : (
            <label>
              <span>Tipo</span>
              <input
                type="text"
                value={TIPO_LABELS[formData.tipo] ?? formData.tipo}
                disabled
                className="perfil-disabled-input"
              />
            </label>
          )}

          {isCreate ? (
            <label>
              <span>
                Unidade primária (controle de estoque){' '}
                <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <select
                name="unidadeMedidaPrimariaId"
                value={formData.unidadeMedidaPrimariaId}
                onChange={onChange}
                required
              >
                <option value="">Selecione...</option>
                {unidades.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.unidade}
                  </option>
                ))}
              </select>
            </label>
          ) : (
            <label>
              <span>Unidade primária</span>
              <input
                type="text"
                value={formData.unidadeMedidaPrimariaSigla}
                disabled
                className="perfil-disabled-input"
              />
            </label>
          )}

          <label>
            <span>Unidade secundária (usada no consumo diário, ex.: KG)</span>
            <select
              name="unidadeMedidaSecundariaId"
              value={formData.unidadeMedidaSecundariaId}
              onChange={onChange}
            >
              <option value="">Nenhuma</option>
              {unidades
                .filter((u) => String(u.id) !== String(formData.unidadeMedidaPrimariaId))
                .map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.unidade}
                  </option>
                ))}
            </select>
          </label>

          {temUnidadeSecundaria ? (
            <label>
              <span>
                Fator de conversão (1 unidade primária = quantas secundárias){' '}
                <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input
                type="number"
                name="fatorConversao"
                value={formData.fatorConversao}
                onChange={onChange}
                min="0.01"
                step="0.01"
                placeholder="Ex.: 40 (1 SACA = 40 KG)"
                required
              />
            </label>
          ) : null}

          <label>
            <span>Estoque mínimo</span>
            <input
              type="number"
              name="estoqueMinimo"
              value={formData.estoqueMinimo}
              onChange={onChange}
              min="0"
              step="0.01"
              placeholder="Alerta quando o saldo ficar abaixo deste valor"
            />
          </label>

          {!isCreate ? (
            <fieldset className="senha-fieldset">
              <legend>Financeiro</legend>
              <p className="form-help">
                Editar diretamente aqui sobrescreve os valores calculados automaticamente
                pelas entradas de estoque. Use apenas para correções pontuais.
              </p>

              <label>
                <span>Preço de compra médio (R$)</span>
                <input
                  type="number"
                  name="precoCompraMedio"
                  value={formData.precoCompraMedio}
                  onChange={onChange}
                  min="0"
                  step="0.01"
                />
              </label>

              <label>
                <span>Preço da última compra (R$)</span>
                <input
                  type="number"
                  name="precoUltimaCompra"
                  value={formData.precoUltimaCompra}
                  onChange={onChange}
                  min="0"
                  step="0.01"
                />
              </label>
            </fieldset>
          ) : null}

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
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

export default InsumoEstoqueFormModal
