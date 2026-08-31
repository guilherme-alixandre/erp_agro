const CRIAR_PRODUTO_VALUE = '__criar_novo__'

function ReciboSimplesFormModal({ formData, produtos, isSaving, feedback, onClose, onChange, onCriarProduto, onSubmit }) {
  function handleProdutoChange(event) {
    if (event.target.value === CRIAR_PRODUTO_VALUE) {
      onCriarProduto()
      return
    }
    onChange(event)
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Novo recibo (documento não fiscal)</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          Recibos não fiscais sempre nascem <strong>pendentes</strong> e só entram no resumo
          financeiro (e no estoque do produto) depois de aprovados por um Administrador ou
          Gerente na aba Aprovações.
        </p>

        <form className="animal-form" onSubmit={onSubmit}>
          <label>
            <span>
              Descrição <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <textarea name="descricao" value={formData.descricao} onChange={onChange} rows={2} required />
          </label>

          <label>
            <span>
              Produto <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="produtoId" value={formData.produtoId} onChange={handleProdutoChange} required>
              <option value="" disabled>Selecione o produto...</option>
              {produtos.map((p) => (
                <option key={p.id} value={p.id}>{p.nome}</option>
              ))}
              <option value={CRIAR_PRODUTO_VALUE}>+ Criar novo produto...</option>
            </select>
          </label>
          <p className="form-help">
            Não encontrou o produto? Selecione &quot;+ Criar novo produto...&quot; para cadastrá-lo agora.
          </p>

          <label>
            <span>
              Quantidade recebida <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="number"
              name="quantidade"
              value={formData.quantidade}
              onChange={onChange}
              min="0.001"
              step="0.001"
              required
            />
          </label>

          <label>
            <span>
              Data de emissão <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input type="date" name="dataEmissao" value={formData.dataEmissao} onChange={onChange} required />
          </label>

          <label>
            <span>
              Valor total (R$) <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="number"
              name="valorTotal"
              value={formData.valorTotal}
              onChange={onChange}
              min="0.01"
              step="0.01"
              required
            />
          </label>

          <label>
            <span>
              Natureza financeira <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="naturezaFinanceira" value={formData.naturezaFinanceira} onChange={onChange} required>
              <option value="" disabled>Selecione...</option>
              <option value="CUSTO">Custo (ligado à produção)</option>
              <option value="GASTO">Gasto (administrativo)</option>
            </select>
          </label>

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

export default ReciboSimplesFormModal
