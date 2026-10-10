import { getCorCategoria } from '../../../utils/chartColors'

/**
 * Barra de ocupação de um setor: fundo neutro = capacidade total, um
 * segmento colorido por lote alocado (proporcional à quantidade de animais
 * daquele lote no setor). Não usa Recharts — cada setor tem um conjunto
 * diferente de lotes, o que não se encaixa no modelo de série compartilhada
 * de um BarChart; um componente CSS/flex dá controle total sobre o
 * segmento neutro e é mais simples.
 *
 * `onClick` — opcional; quando informado, a barra inteira fica clicável
 * (filtra a tabela de setores abaixo por este setor). `isActive` destaca a
 * barra quando esse filtro está em uso.
 */
function SetorOcupacaoBar({ setor, onClick, isActive }) {
  const capacidade = Math.max(0, setor.capacidadeMaxima ?? 0)
  const lotes = (setor.lotes ?? []).filter((l) => (l.quantidadeAnimais ?? 0) > 0)
  const ocupados = lotes.reduce((acc, l) => acc + (l.quantidadeAnimais ?? 0), 0)
  const semLimite = capacidade === 0

  // Base defensiva: a barra nunca "estoura" visualmente, mesmo se
  // ocupados > capacidade (não deveria acontecer, dada a validação do
  // backend) ou se o setor não tiver limite cadastrado (capacidade = 0).
  const base = Math.max(capacidade, ocupados, 1)
  const livre = Math.max(base - ocupados, 0)

  const resumoTexto = semLimite
    ? `${ocupados} ${ocupados === 1 ? 'animal' : 'animais'} (sem limite)`
    : `${ocupados}/${capacidade} animais`

  return (
    <div
      className={`setor-ocupacao-bar ${isActive ? 'setor-ocupacao-bar--ativo' : ''}`}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      onClick={onClick}
      onKeyDown={
        onClick
          ? (event) => {
              if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault()
                onClick()
              }
            }
          : undefined
      }
    >
      <div className="setor-ocupacao-bar__legenda">
        <strong>{setor.nome}</strong>
        <span>{resumoTexto}</span>
      </div>

      <div
        className="setor-ocupacao-bar__track"
        role="img"
        aria-label={`Ocupação do setor ${setor.nome}: ${resumoTexto}${
          lotes.length > 0
            ? `, por lote: ${lotes.map((l) => `${l.loteCodigo} com ${l.quantidadeAnimais}`).join(', ')}`
            : ''
        }`}
      >
        {lotes.map((lote) => {
          const pct = (lote.quantidadeAnimais / base) * 100
          return (
            <div
              key={lote.loteSectorId ?? lote.loteId}
              className="setor-ocupacao-bar__segment"
              style={{ width: `${pct}%`, background: getCorCategoria(lote.loteId) }}
              title={`${lote.loteCodigo}: ${lote.quantidadeAnimais} ${lote.quantidadeAnimais === 1 ? 'animal' : 'animais'}`}
              tabIndex={0}
            >
              {pct >= 12 ? <span className="setor-ocupacao-bar__segment-label">{lote.loteCodigo}</span> : null}
            </div>
          )
        })}
        {livre > 0 ? (
          <div
            className="setor-ocupacao-bar__segment setor-ocupacao-bar__segment--livre"
            style={{ width: `${(livre / base) * 100}%` }}
            title={`${livre} ${livre === 1 ? 'vaga livre' : 'vagas livres'}`}
          />
        ) : null}
      </div>

      {lotes.length > 0 ? (
        <ul className="setor-ocupacao-bar__chips">
          {lotes.map((lote) => (
            <li key={lote.loteSectorId ?? lote.loteId} className="setor-ocupacao-bar__chip">
              <span
                className="setor-ocupacao-bar__chip-swatch"
                style={{ background: getCorCategoria(lote.loteId) }}
                aria-hidden="true"
              />
              {lote.loteCodigo}: {lote.quantidadeAnimais}
            </li>
          ))}
        </ul>
      ) : (
        <p className="setor-ocupacao-bar__vazio">Nenhum lote alocado.</p>
      )}
    </div>
  )
}

export default SetorOcupacaoBar
