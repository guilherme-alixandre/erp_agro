import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

function formatarMoeda(valor) {
  return `R$ ${Number(valor ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

function formatarEixoY(valor) {
  const numero = Number(valor ?? 0)
  return Math.abs(numero) >= 1000 ? `R$ ${(numero / 1000).toFixed(0)}k` : `R$ ${numero.toFixed(0)}`
}

/** data: [{ label: 'MM/AAAA', Receita, Custo, Despesa }, ...] — mais antigo primeiro. */
function DreLineChart({ data }) {
  return (
    <ResponsiveContainer width="100%" height={340}>
      <LineChart data={data} margin={{ top: 8, right: 24, left: 8, bottom: 8 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0" />
        <XAxis dataKey="label" tick={{ fontSize: 12 }} />
        <YAxis tick={{ fontSize: 12 }} tickFormatter={formatarEixoY} />
        <Tooltip formatter={(valor) => formatarMoeda(valor)} />
        <Legend />
        <Line type="monotone" dataKey="Receita" stroke="#1a7f37" strokeWidth={2} dot={{ r: 3 }} />
        <Line type="monotone" dataKey="Custo" stroke="#b45309" strokeWidth={2} dot={{ r: 3 }} />
        <Line type="monotone" dataKey="Despesa" stroke="#b42318" strokeWidth={2} dot={{ r: 3 }} />
      </LineChart>
    </ResponsiveContainer>
  )
}

export default DreLineChart
