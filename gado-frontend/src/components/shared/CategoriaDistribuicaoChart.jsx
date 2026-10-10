import { Bar, BarChart, Cell, LabelList, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { getCorCategoria } from '../../utils/chartColors'

/**
 * Barra horizontal — uma cor por categoria (via getCorCategoria), ordenada
 * pelo chamador. Usada para "quantos de cada X existem" (raças, grupos de
 * insumo): poucas categorias nomeadas, comparação direta de magnitude.
 *
 * `onBarClick(nome)` — opcional; quando informado, cada barra fica clicável
 * (cursor de ponteiro) e `activeNome` pode destacar a barra correspondente,
 * apagando levemente as demais, para indicar um filtro ativo.
 */
function CategoriaDistribuicaoChart({ data, unidadeLabel = 'itens', onBarClick, activeNome }) {
  if (!data || data.length === 0) {
    return <p className="chart-card__empty">Sem dados para exibir.</p>
  }

  const altura = Math.max(220, data.length * 44)

  return (
    <ResponsiveContainer width="100%" height={altura}>
      <BarChart
        data={data}
        layout="vertical"
        margin={{ top: 8, right: 48, left: 8, bottom: 8 }}
        barCategoryGap={10}
      >
        <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
        <YAxis type="category" dataKey="nome" width={130} tick={{ fontSize: 12 }} />
        <Tooltip formatter={(valor) => [`${valor} ${unidadeLabel}`, '']} />
        <Bar
          dataKey="quantidade"
          radius={[0, 4, 4, 0]}
          maxBarSize={24}
          onClick={onBarClick ? (item) => onBarClick(item.nome) : undefined}
          cursor={onBarClick ? 'pointer' : undefined}
        >
          {data.map((item) => (
            <Cell
              key={item.nome}
              fill={getCorCategoria(item.nome)}
              fillOpacity={!activeNome || activeNome === item.nome ? 1 : 0.35}
            />
          ))}
          <LabelList dataKey="quantidade" position="right" style={{ fill: 'var(--color-ink)' }} />
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  )
}

export default CategoriaDistribuicaoChart
