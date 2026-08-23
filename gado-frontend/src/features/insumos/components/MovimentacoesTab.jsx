import { useCallback, useEffect, useState } from 'react'
import { listarMovimentacoes, TIPO_LABEL } from '../integration/movimentacaoEstoqueApi'

function formatarData(iso) {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return iso
  return data.toLocaleString('pt-BR')
}

function origemDestino(m) {
  if (m.parceiroNome) return m.parceiroNome
  if (m.setorNome) return `Setor: ${m.setorNome}`
  if (m.animalCodigoBrinco) return `Animal: ${m.animalCodigoBrinco}`
  return '—'
}

function MovimentacoesTab() {
  const [movimentacoes, setMovimentacoes] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const fetchMovimentacoes = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const lista = await listarMovimentacoes()
      setMovimentacoes(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar as movimentações.' })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchMovimentacoes()
  }, [fetchMovimentacoes])

  return (
    <>
      <p className="form-info">
        Histórico de movimentações de estoque: entradas (compras), saídas (consumo, alimentação
        de setores, venda/abate de animais) e aplicações (vacinação).
      </p>

      {feedback.message ? (
        <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
          {feedback.message}
        </p>
      ) : null}

      <div className="data-toolbar">
        <p className="animals-count">
          {isLoading ? 'Carregando...' : `${movimentacoes.length} movimentação(ões)`}
        </p>
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Data</th>
              <th>Tipo</th>
              <th>Produto</th>
              <th>Quantidade</th>
              <th>Valor Unitário</th>
              <th>Origem/Destino</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={6} className="table-loading">Carregando...</td></tr>
            ) : movimentacoes.length === 0 ? (
              <tr><td colSpan={6} className="table-empty">Nenhuma movimentação registrada.</td></tr>
            ) : (
              movimentacoes.map((m) => (
                <tr key={m.id}>
                  <td>{formatarData(m.dataMovimentacao)}</td>
                  <td>{TIPO_LABEL[m.tipo] ?? m.tipo}</td>
                  <td>{m.insumoNome || '—'}</td>
                  <td>{m.quantidade} {m.unidadeMedidaSigla}</td>
                  <td>R$ {Number(m.valorUnitario ?? 0).toFixed(2)}</td>
                  <td>{origemDestino(m)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </>
  )
}

export default MovimentacoesTab
