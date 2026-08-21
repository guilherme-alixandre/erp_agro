import { useCallback, useEffect, useState } from 'react'
import { gerarResumoMensal, gerarResumoUltimosMeses } from '../integration/lancamentoFinanceiroApi'
import DreLineChart from './DreLineChart'

const agora = new Date()
const MESES_ABREV = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']

function formatarMoeda(valor) {
  return `R$ ${Number(valor ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function DashboardTab({ currentUser }) {
  const [bloco, setBloco] = useState({ ano: agora.getFullYear(), mes: agora.getMonth() + 1 })
  const [resumo, setResumo] = useState(null)
  const [chartData, setChartData] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState('')

  const fetchDados = useCallback(async () => {
    setIsLoading(true)
    setFeedback('')
    try {
      const [resumoMes, ultimosMeses] = await Promise.all([
        gerarResumoMensal(currentUser.email, bloco.ano, bloco.mes),
        gerarResumoUltimosMeses(currentUser.email, bloco.ano, bloco.mes, 6),
      ])
      setResumo(resumoMes)
      setChartData(
        ultimosMeses.map((r) => ({
          label: `${MESES_ABREV[r.mes - 1]}/${String(r.ano).slice(2)}`,
          Receita: r.totalEntradas,
          Custo: r.totalSaidasCusto,
          Despesa: r.totalSaidasDespesa,
        })),
      )
    } catch (error) {
      setFeedback(error.message || 'Falha ao carregar o resumo financeiro.')
    } finally {
      setIsLoading(false)
    }
  }, [currentUser.email, bloco])

  useEffect(() => {
    fetchDados()
  }, [fetchDados])

  const lucroPositivo = (resumo?.lucroLiquido ?? 0) >= 0

  return (
    <>
      <div className="data-toolbar">
        <p className="animals-count">Bloco Ano/Mês</p>
        <div className="financeiro-bloco-selector">
          <select value={bloco.mes} onChange={(e) => setBloco((c) => ({ ...c, mes: Number(e.target.value) }))}>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((mes) => (
              <option key={mes} value={mes}>{MESES_ABREV[mes - 1]}</option>
            ))}
          </select>
          <input
            type="number"
            value={bloco.ano}
            onChange={(e) => setBloco((c) => ({ ...c, ano: Number(e.target.value) }))}
            style={{ width: '90px' }}
          />
        </div>
      </div>

      {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

      <div className="financeiro-kpi-grid">
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Total Entradas</span>
          <strong className="financeiro-kpi-card__value financeiro-kpi-card__value--positivo">
            {isLoading ? '...' : formatarMoeda(resumo?.totalEntradas)}
          </strong>
        </div>
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Saídas — Custo</span>
          <strong className="financeiro-kpi-card__value">
            {isLoading ? '...' : formatarMoeda(resumo?.totalSaidasCusto)}
          </strong>
        </div>
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Saídas — Despesa</span>
          <strong className="financeiro-kpi-card__value">
            {isLoading ? '...' : formatarMoeda(resumo?.totalSaidasDespesa)}
          </strong>
        </div>
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Lucro Líquido</span>
          <strong
            className={`financeiro-kpi-card__value ${lucroPositivo ? 'financeiro-kpi-card__value--positivo' : 'financeiro-kpi-card__value--negativo'}`}
          >
            {isLoading ? '...' : formatarMoeda(resumo?.lucroLiquido)}
          </strong>
        </div>
      </div>

      <div className="financeiro-chart-card">
        <h3>Custo vs Despesa vs Receita — últimos 6 meses</h3>
        <DreLineChart data={chartData} />
      </div>
    </>
  )
}

export default DashboardTab
