import { useEffect, useState } from 'react'
import { listarEstoque } from '../../insumos/integration/insumoApi'

const NATUREZA_LABEL = { CUSTO: 'Custo', GASTO: 'Gasto' }

/**
 * Liga cada item importado ao produto do catálogo correspondente. Deliberadamente não expõe
 * quantidade/valor — só o produtoId — reforçando que vincular nunca altera valores do documento
 * (regra de negócio: Financeiro pode vincular, só Admin/Gerente edita valores, em EditarNfeModal).
 */
function VincularItensModal({ documento, onClose, onVincular, vinculandoItemId, feedbackPorItem }) {
  const [produtos, setProdutos] = useState([])
  const [selecao, setSelecao] = useState({})

  useEffect(() => {
    listarEstoque('').then(setProdutos).catch(() => setProdutos([]))
  }, [])

  function handleSelecaoChange(itemId, produtoId) {
    setSelecao((current) => ({ ...current, [itemId]: produtoId }))
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>Vincular itens ao catálogo</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          {documento.tipoDocumento === 'NF_E' ? 'NF-e' : 'Recibo'} {documento.numeroDocumento || ''} —
          ligue cada item importado a um produto do catálogo. A natureza (Custo/Gasto) do item é
          herdada automaticamente do grupo do produto vinculado.
        </p>

        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Descrição (do XML)</th>
                <th>Qtd.</th>
                <th>Valor Total</th>
                <th>Produto do catálogo</th>
                <th>Natureza</th>
                <th>Ação</th>
              </tr>
            </thead>
            <tbody>
              {documento.itens.map((item) => (
                <tr key={item.id}>
                  <td>{item.descricaoXml}</td>
                  <td>{item.quantidade}</td>
                  <td>R$ {Number(item.valorTotal ?? 0).toFixed(2)}</td>
                  <td>
                    {item.vinculado ? (
                      item.produtoNome
                    ) : (
                      <select
                        value={selecao[item.id] ?? ''}
                        onChange={(e) => handleSelecaoChange(item.id, e.target.value)}
                      >
                        <option value="">Selecione...</option>
                        {produtos.map((p) => (
                          <option key={p.id} value={p.id}>
                            {p.codigoProduto ? `${p.codigoProduto} - ` : ''}{p.nome}
                          </option>
                        ))}
                      </select>
                    )}
                  </td>
                  <td>{item.naturezaFinanceira ? NATUREZA_LABEL[item.naturezaFinanceira] : '—'}</td>
                  <td>
                    {item.vinculado ? (
                      <span className="financeiro-status financeiro-status--aprovado">Vinculado</span>
                    ) : (
                      <button
                        type="button"
                        className="btn-row"
                        disabled={!selecao[item.id] || vinculandoItemId === item.id}
                        onClick={() => onVincular(item.id, selecao[item.id])}
                      >
                        {vinculandoItemId === item.id ? 'Vinculando...' : 'Vincular'}
                      </button>
                    )}
                    {feedbackPorItem?.[item.id] ? (
                      <p className="feedback feedback--error financeiro-item-feedback">
                        {feedbackPorItem[item.id]}
                      </p>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={onClose}>
            Fechar
          </button>
        </div>
      </div>
    </div>
  )
}

export default VincularItensModal
