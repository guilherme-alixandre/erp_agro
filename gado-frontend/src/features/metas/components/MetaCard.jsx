import { useState } from 'react'
import { deletarMedicao, formatarMoeda, formatarNumero, unidadeMeta, TIPOS_GADO } from '../integration/metaSetorApi'
import MedicaoModal from './MedicaoModal'

/**
 * Card full-width para uma MetaSetor.
 *
 * Props:
 *  - meta          → objeto MetaSetor normalizado
 *  - lotes         → lista de lotes do setor, para o modal de medição
 *  - currentUser   → usuário logado { email, perfil, ... }
 *  - podeGerenciar → boolean — ADMINISTRADOR ou GERENTE
 *  - onEditar      → fn(meta) — abre modal de edição
 *  - onDeletar     → fn(meta) — solicita exclusão
 *  - onRefresh     → fn() — recarrega a lista após mudança
 */
function MetaCard({ meta, lotes, currentUser, podeGerenciar, onEditar, onDeletar, onRefresh }) {
  const emailUsuario = currentUser.email
  const perfil = currentUser.perfil

  const [showMedicoes, setShowMedicoes] = useState(false)
  const [showMedicaoModal, setShowMedicaoModal] = useState(false)
  const [medicaoEditando, setMedicaoEditando] = useState(null)
  const [deletandoMedicao, setDeletandoMedicao] = useState(null)

  function podeEditarMedicao(medicao) {
    if (perfil === 'ADMINISTRADOR' || perfil === 'GERENTE') return true
    if (perfil === 'CUIDADOR_CHEFE') {
      const perfilCriador = medicao.criadoPorPerfil
      return perfilCriador === 'CUIDADOR' || perfilCriador === 'CUIDADOR_CHEFE'
    }
    if (perfil === 'CUIDADOR') return medicao.criadoPorEmail === emailUsuario
    return false
  }

  const pct = Math.min(meta.percentualProgresso ?? 0, 100)
  const pctReal = meta.percentualProgresso ?? 0
  const barClass =
    pctReal >= 100 ? 'meta-progress__bar-fill--over' : pctReal >= 70 ? '' : 'meta-progress__bar-fill--warning'

  const temVendido = meta.tipoMeta === 'LEITE' && meta.quantidadeVendida != null
  const pctVendido = Math.min(meta.percentualVendido ?? 0, 100)

  const tipoGadoLabel = meta.tipoGado ? TIPOS_GADO.find((t) => t.value === meta.tipoGado)?.label ?? meta.tipoGado : null
  const unidade = unidadeMeta(meta.tipoMeta)

  function fmtData(iso) {
    if (!iso) return '—'
    const [y, m, d] = iso.split('-')
    return `${d}/${m}/${y}`
  }

  async function handleDeletarMedicao(medicaoId) {
    if (!window.confirm('Remover esta medição?')) return
    setDeletandoMedicao(medicaoId)
    try {
      await deletarMedicao(medicaoId, emailUsuario)
      onRefresh()
    } catch {
      // falha silenciosa — onRefresh também tolera erro
    } finally {
      setDeletandoMedicao(null)
    }
  }

  function handleMedicaoSalva() {
    setShowMedicaoModal(false)
    setMedicaoEditando(null)
    onRefresh()
  }

  return (
    <>
      <article className="meta-card">
        <div className="meta-card__header">
          <div className="meta-card__title">
            <h3>{meta.setorNome}</h3>
            <span className="meta-card__subtitle">
              {fmtData(meta.dataInicial)} — {fmtData(meta.dataFinal)}
              {tipoGadoLabel ? <> · {tipoGadoLabel}</> : null}
            </span>
          </div>
          <span className={`meta-card__badge ${meta.tipoMeta === 'LEITE' ? 'meta-card__badge--leite' : 'meta-card__badge--arroba'}`}>
            {meta.tipoMeta === 'LEITE' ? '🥛 Leite' : '⚖️ Arroba'}
          </span>
        </div>

        <div className="meta-progress">
          <div className="meta-progress__header">
            <span className="meta-progress__label">
              {formatarNumero(meta.quantidadeRealizada)} {unidade} de {formatarNumero(meta.quantidadeEsperada)} {unidade}
            </span>
            <span className="meta-progress__pct">{formatarNumero(pctReal, 1)}%</span>
          </div>
          <div className="meta-progress__bar-track">
            <div
              className={`meta-progress__bar-fill ${barClass}`}
              style={{ width: `${pct}%` }}
              role="progressbar"
              aria-valuenow={pctReal}
              aria-valuemin={0}
              aria-valuemax={100}
            />
          </div>

          {temVendido ? (
            <>
              <div className="meta-progress__header">
                <span className="meta-progress__label">
                  {formatarNumero(meta.quantidadeVendida)} {unidade} vendidos
                </span>
                <span className="meta-progress__pct">{formatarNumero(pctVendido, 1)}%</span>
              </div>
              <div className="meta-progress__bar-track">
                <div
                  className="meta-progress__bar-fill meta-progress__bar-fill--vendido"
                  style={{ width: `${pctVendido}%` }}
                  role="progressbar"
                  aria-valuenow={pctVendido}
                  aria-valuemin={0}
                  aria-valuemax={100}
                />
              </div>
            </>
          ) : null}
        </div>

        <div className="meta-stats">
          <div className="meta-stat">
            <div className="meta-stat__label">Realizado</div>
            <div className="meta-stat__value">{formatarMoeda(meta.valorRealizado)}</div>
          </div>
          <div className="meta-stat">
            <div className="meta-stat__label">Meta</div>
            <div className="meta-stat__value">{formatarMoeda(meta.valorEsperado)}</div>
          </div>
          <div className="meta-stat">
            <div className="meta-stat__label">Preço médio</div>
            <div className="meta-stat__value">
              {formatarMoeda(meta.precoMedio)}/{unidade}
            </div>
          </div>
          <div className="meta-stat">
            <div className="meta-stat__label">Medições</div>
            <div className="meta-stat__value">{meta.medicoes.length}</div>
          </div>
          {temVendido ? (
            <div className="meta-stat">
              <div className="meta-stat__label">Vendido</div>
              <div className="meta-stat__value">
                {formatarNumero(meta.quantidadeVendida)} {unidade}
              </div>
            </div>
          ) : null}
        </div>

        <div className="meta-medicoes">
          <button type="button" className="meta-medicoes__toggle" onClick={() => setShowMedicoes((v) => !v)}>
            {showMedicoes ? '▾' : '▸'} Histórico de medições ({meta.medicoes.length})
          </button>

          {showMedicoes ? (
            meta.medicoes.length > 0 ? (
              <table className="meta-medicoes__table">
                <thead>
                  <tr>
                    <th>Data</th>
                    <th>Lote</th>
                    <th>Lançado</th>
                    <th>Convertido</th>
                    <th>Criado por</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {meta.medicoes.map((m) => (
                    <tr key={m.id}>
                      <td>{fmtData(m.dataMedicao)}</td>
                      <td>{m.loteCodigo || m.loteDescricao}</td>
                      <td>
                        {formatarNumero(m.quantidadeLancada)} {meta.tipoMeta === 'LEITE' ? 'L' : 'Kg'}
                      </td>
                      <td>
                        {formatarNumero(m.quantidadeConvertida)} {unidade}
                      </td>
                      <td>{m.criadoPorNome || m.criadoPorEmail || '—'}</td>
                      <td>
                        {podeEditarMedicao(m) ? (
                          <button
                            type="button"
                            className="btn-edit-medicao"
                            onClick={() => setMedicaoEditando(m)}
                            aria-label="Editar medição"
                          >
                            ✎
                          </button>
                        ) : null}
                        {podeEditarMedicao(m) ? (
                          <button
                            type="button"
                            className="btn-del-medicao"
                            onClick={() => handleDeletarMedicao(m.id)}
                            disabled={deletandoMedicao === m.id}
                            aria-label="Remover medição"
                          >
                            {deletandoMedicao === m.id ? '...' : '✕'}
                          </button>
                        ) : null}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="meta-medicoes__empty">Nenhuma medição registrada ainda.</p>
            )
          ) : null}
        </div>

        <div className="meta-card__actions">
          <button type="button" className="btn-primary" onClick={() => setShowMedicaoModal(true)}>
            + Adicionar medição
          </button>

          {podeGerenciar ? (
            <>
              <button type="button" className="btn-secondary" onClick={() => onEditar(meta)}>
                Editar meta
              </button>
              <button type="button" className="btn-danger" onClick={() => onDeletar(meta)}>
                Excluir
              </button>
            </>
          ) : null}
        </div>
      </article>

      {showMedicaoModal ? (
        <MedicaoModal
          meta={meta}
          lotes={lotes}
          emailUsuario={emailUsuario}
          onClose={() => setShowMedicaoModal(false)}
          onSaved={handleMedicaoSalva}
        />
      ) : null}

      {medicaoEditando ? (
        <MedicaoModal
          meta={meta}
          lotes={lotes}
          emailUsuario={emailUsuario}
          medicaoParaEditar={medicaoEditando}
          onClose={() => setMedicaoEditando(null)}
          onSaved={handleMedicaoSalva}
        />
      ) : null}
    </>
  )
}

export default MetaCard
