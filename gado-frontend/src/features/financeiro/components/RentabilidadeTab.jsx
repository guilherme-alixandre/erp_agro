import { useCallback, useEffect, useRef, useState } from 'react'
import {
  gerarResumoPeriodo,
  listarContasPorParceiro,
  listarVendasPorProduto,
  listarGastosPorGrupo,
  exportarRentabilidadePDF,
  exportarContasParceiroCSV,
} from '../integration/rentabilidadeApi'
import { formatarMoeda, formatarData } from '../../../utils/formatters'
import DreLineChart from './DreLineChart'
import CategoriaDistribuicaoChart from '../../../components/shared/CategoriaDistribuicaoChart'

const MESES_ABREV = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez']

function todayIso() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

function startOfYearIso() {
  return `${new Date().getFullYear()}-01-01`
}

function RentabilidadeTab({ currentUser }) {
  const [periodo, setPeriodo] = useState({ dataInicio: startOfYearIso(), dataFim: todayIso() })
  const [resumo, setResumo] = useState(null)
  const [contasParceiro, setContasParceiro] = useState([])
  const [vendasProduto, setVendasProduto] = useState([])
  const [gastosGrupo, setGastosGrupo] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState('')
  const [exportMenuOpen, setExportMenuOpen] = useState(false)
  const [isExportingPdf, setIsExportingPdf] = useState(false)
  const exportMenuRef = useRef(null)

  useEffect(() => {
    if (!exportMenuOpen) return
    function handleClickOutside(event) {
      if (exportMenuRef.current && !exportMenuRef.current.contains(event.target)) {
        setExportMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [exportMenuOpen])

  const fetchDados = useCallback(async () => {
    if (!periodo.dataInicio || !periodo.dataFim) return
    setIsLoading(true)
    setFeedback('')
    try {
      const [resumoPeriodo, contas, vendas, gastos] = await Promise.all([
        gerarResumoPeriodo(currentUser.email, periodo.dataInicio, periodo.dataFim),
        listarContasPorParceiro(currentUser.email, periodo.dataInicio, periodo.dataFim),
        listarVendasPorProduto(currentUser.email, periodo.dataInicio, periodo.dataFim),
        listarGastosPorGrupo(currentUser.email, periodo.dataInicio, periodo.dataFim),
      ])
      setResumo(resumoPeriodo)
      setContasParceiro(contas)
      setVendasProduto(vendas)
      setGastosGrupo(gastos)
    } catch (error) {
      setFeedback(error.message || 'Falha ao carregar os dados de rentabilidade.')
    } finally {
      setIsLoading(false)
    }
  }, [currentUser.email, periodo])

  useEffect(() => {
    fetchDados()
  }, [fetchDados])

  function handleExportarCSV() {
    exportarContasParceiroCSV(contasParceiro)
    setExportMenuOpen(false)
  }

  function handleExportarPDF() {
    setExportMenuOpen(false)
    setIsExportingPdf(true)
    exportarRentabilidadePDF(periodo.dataInicio, periodo.dataFim)
      .catch((error) => setFeedback(error.message || 'Falha ao gerar o PDF de rentabilidade.'))
      .finally(() => setIsExportingPdf(false))
  }

  const lucroPositivo = (resumo?.lucroLiquido ?? 0) >= 0

  const chartDataMensal = (resumo?.porMes ?? []).map((m) => ({
    label: `${MESES_ABREV[m.mes - 1]}/${String(m.ano).slice(2)}`,
    Receita: m.totalEntradas,
    Custo: m.totalSaidasCusto,
    Despesa: m.totalSaidasDespesa,
  }))

  const dadosChartParceiros = contasParceiro.map((c) => ({ nome: c.parceiroNome, quantidade: c.totalAPagar + c.totalAReceber }))
  const dadosChartProdutos = vendasProduto.map((p) => ({ nome: p.nome, quantidade: p.valorTotal }))
  const dadosChartGastos = gastosGrupo.map((g) => ({ nome: g.grupoNome, quantidade: g.valorTotal }))

  return (
    <>
      <div className="data-toolbar">
        <label className="toolbar-date-label">
          <span className="toolbar-date-label__text">De</span>
          <input
            type="date"
            className="toolbar-date"
            value={periodo.dataInicio}
            max={periodo.dataFim || undefined}
            onChange={(e) => setPeriodo((c) => ({ ...c, dataInicio: e.target.value }))}
          />
        </label>
        <label className="toolbar-date-label">
          <span className="toolbar-date-label__text">Até</span>
          <input
            type="date"
            className="toolbar-date"
            value={periodo.dataFim}
            min={periodo.dataInicio || undefined}
            onChange={(e) => setPeriodo((c) => ({ ...c, dataFim: e.target.value }))}
          />
        </label>

        <div className="export-wrapper" ref={exportMenuRef}>
          <button type="button" className="btn-export-csv" onClick={() => setExportMenuOpen((v) => !v)}>
            Exportar ▾
          </button>
          {exportMenuOpen ? (
            <div className="export-menu">
              <button type="button" className="export-menu__item" onClick={handleExportarCSV}>
                Exportar contas (CSV)
              </button>
              <hr className="export-menu__separator" />
              <button type="button" className="export-menu__item" onClick={handleExportarPDF} disabled={isExportingPdf}>
                {isExportingPdf ? 'Gerando PDF...' : 'Exportar relatório (PDF)'}
              </button>
            </div>
          ) : null}
        </div>
      </div>

      {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

      <div className="financeiro-kpi-grid">
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Total de entradas</span>
          <strong className="financeiro-kpi-card__value financeiro-kpi-card__value--positivo">
            {isLoading ? '...' : formatarMoeda(resumo?.totalEntradas)}
          </strong>
        </div>
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Saídas — custo</span>
          <strong className="financeiro-kpi-card__value">
            {isLoading ? '...' : formatarMoeda(resumo?.totalSaidasCusto)}
          </strong>
        </div>
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Saídas — despesa</span>
          <strong className="financeiro-kpi-card__value">
            {isLoading ? '...' : formatarMoeda(resumo?.totalSaidasDespesa)}
          </strong>
        </div>
        <div className="financeiro-kpi-card">
          <span className="financeiro-kpi-card__label">Lucro líquido</span>
          <strong
            className={`financeiro-kpi-card__value ${lucroPositivo ? 'financeiro-kpi-card__value--positivo' : 'financeiro-kpi-card__value--negativo'}`}
          >
            {isLoading ? '...' : formatarMoeda(resumo?.lucroLiquido)}
          </strong>
        </div>
      </div>

      <div className="financeiro-chart-card">
        <h3>Custo, despesa e receita no período</h3>
        <DreLineChart data={chartDataMensal} />
      </div>

      <div className="chart-card">
        <h3>Contas a Pagar e Receber por Parceiro</h3>
        <CategoriaDistribuicaoChart data={dadosChartParceiros} formatValor={formatarMoeda} />
        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Parceiro</th>
                <th>A Pagar</th>
                <th>A Receber</th>
                <th>Próx. Vencimento</th>
              </tr>
            </thead>
            <tbody>
              {contasParceiro.length === 0 ? (
                <tr>
                  <td colSpan={4} className="table-empty">Nenhuma conta com vencimento neste período.</td>
                </tr>
              ) : (
                contasParceiro.map((c) => (
                  <tr key={c.parceiroId}>
                    <td>{c.parceiroNome}</td>
                    <td>{formatarMoeda(c.totalAPagar)}</td>
                    <td>{formatarMoeda(c.totalAReceber)}</td>
                    <td>{formatarData(c.proximoVencimento)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="chart-card">
        <h3>Produtos Mais Vendidos</h3>
        <CategoriaDistribuicaoChart data={dadosChartProdutos} formatValor={formatarMoeda} />
        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Produto</th>
                <th>Quantidade</th>
                <th>Valor Total</th>
              </tr>
            </thead>
            <tbody>
              {vendasProduto.length === 0 ? (
                <tr>
                  <td colSpan={3} className="table-empty">Nenhuma venda neste período.</td>
                </tr>
              ) : (
                vendasProduto.map((p) => (
                  <tr key={p.nome}>
                    <td>{p.nome}</td>
                    <td>{Number(p.quantidade).toLocaleString('pt-BR')} {p.unidade}</td>
                    <td>{formatarMoeda(p.valorTotal)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      <div className="chart-card">
        <h3>Maiores Gastos por Grupo</h3>
        <CategoriaDistribuicaoChart data={dadosChartGastos} formatValor={formatarMoeda} />
        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Grupo</th>
                <th>Valor Total</th>
              </tr>
            </thead>
            <tbody>
              {gastosGrupo.length === 0 ? (
                <tr>
                  <td colSpan={2} className="table-empty">Nenhum gasto neste período.</td>
                </tr>
              ) : (
                gastosGrupo.map((g) => (
                  <tr key={g.grupoNome}>
                    <td>{g.grupoNome}</td>
                    <td>{formatarMoeda(g.valorTotal)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}

export default RentabilidadeTab
