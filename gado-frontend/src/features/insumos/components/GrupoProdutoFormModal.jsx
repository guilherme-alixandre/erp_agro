function RequiredLabel({ children }) {
  return (
    <span>
      {children}{' '}
      <span className="required-marker" aria-hidden="true">
        *
      </span>
    </span>
  )
}

function GrupoProdutoFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  onClose,
  onChange,
  onSubmit,
}) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar grupo de produto' : 'Editar grupo de produto'
  const submitText = isSaving ? 'Salvando...' : isCreate ? 'Cadastrar' : 'Salvar alterações'

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="setor-form" onSubmit={onSubmit}>
          <label>
            <RequiredLabel>Nome</RequiredLabel>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              placeholder="Ex.: Animais, Vacinas, Ração"
              required
            />
          </label>

          <label>
            <RequiredLabel>Prefixo (2 dígitos)</RequiredLabel>
            <input
              type="text"
              name="codigoPrefixo"
              value={formData.codigoPrefixo}
              onChange={onChange}
              placeholder="Ex.: 01"
              maxLength={2}
              pattern="\d{2}"
              title="Informe exatamente 2 dígitos numéricos"
              required
            />
          </label>
          <p className="form-help">
            Define os 2 primeiros dígitos do código dos produtos deste grupo (ex.: 01000001).
            Trocar o prefixo de um grupo já em uso não reescreve os códigos já emitidos.
          </p>

          <label>
            <RequiredLabel>Natureza financeira</RequiredLabel>
            <select
              name="naturezaFinanceira"
              value={formData.naturezaFinanceira}
              onChange={onChange}
              required
            >
              <option value="" disabled>
                Selecione...
              </option>
              <option value="CUSTO">Custo</option>
              <option value="GASTO">Gasto</option>
            </select>
          </label>
          <p className="form-help">
            <strong>Custo</strong> é todo gasto que gera retorno financeiro em produtos
            vendidos (ex.: ração, vacinas — entram no preço do que é vendido).{' '}
            <strong>Gasto</strong> não gera retorno direto (ex.: manutenção, insumos gerais —
            são despesas da operação).
          </p>

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

export default GrupoProdutoFormModal
