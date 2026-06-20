import { useState } from 'react'
import {
  deletarMedicao,
  formatarMoeda,
  formatarNumero,
  unidadeMeta,
  TIPOS_GADO,
} from '../../../services/metaSetorApi'
import MedicaoModal from './MedicaoModal'

/**
 * Card full-width para uma MetaSetor.
 *
 * Props:
 *  - meta          → objeto MetaSetor normalizado
 *  - lotes         → lista [{ id, descricao }] do setor
 *  - currentUser   → objeto do usuário logado { email, perfil, ... }
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

  // ── Barra de progresso ───────────────────────────────────────────────
  const pct = Math.min(meta.percentualProgresso ?? 0, 100)
  const pctReal = meta.percentualProgresso ?? 0
  const barClass =
    pctReal >= 100
      ? 'meta-progress__bar-fill--over'
      : pctReal >= 70
      ? ''
      : 'meta-progress__bar-fill--warning'

  // ── Label do tipo de gado ────────────────────────────────────────────
  const tipoGadoLabel = meta.tipoGado
    ? TIPOS_GADO.find((t) => t.value === meta.tipoGado)?.label ?? meta.tipoGado
    : null

  const unidade = unidadeMeta(meta.tipoMeta)

  // ── Datas formatadas ─────────────────────────────────────────────────
  function fmtData(iso) {
    if (!iso) return '—'
    const [y, m, d] = iso.split('-')
    return `${d}/${m}/${y}`
  }

  // ── Deletar medição individual ───────────────────────────────────────
  async function handleDeletarMedicao(medicaoId) {
    if (!window.confirm('Remover esta medição?')) return
    setDeletandoMedicao(medicaoId)
    try {
      await deletarMedicao(medicaoId, emailUsuario)
      onRefresh()
    } catch {
      // erro silencioso — o onRefresh também falha graciosamente na página
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

        {/* ── Cabeçalho ──────────────────────────────────────────────── */}
        <div className="meta-card__header">
          <div className="meta-card__title">
            <h3>{meta.setorNome}</h3>
            <span className="meta-card__subtitle">
              {fmtData(meta.dataInicial)} — {fmtData(meta.dataFinal)}
              {tipoGadoLabel && (
                <> · {tipoGadoLabel}</>
              )}
            </span>
          </div>
          <span
            className={`meta-card__badge ${
              meta.tipoMeta === 'LEITE'
                ? 'meta-card__badge--leite'
                : 'meta-card__badge--arroba'
            }`}
          >
            {meta.tipoMeta === 'LEITE' ? '🥛 Leite' : '⚖️ Arroba'}
          </span>
        </div>

        {/* ── Barra de progresso ─────────────────────────────────────── */}
        <div className="meta-progress">
          <div className="meta-progress__header">
            <span className="meta-progress__label">
              {formatarNumero(meta.quantidadeRealizada)} {unidade} de{' '}
              {formatarNumero(meta.quantidadeEsperada)} {unidade}
            </span>
            <span className="meta-progress__pct">
              {formatarNumero(pctReal, 1)}%
            </span>
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
        </div>

        {/* ── Stats ──────────────────────────────────────────────────── */}
        <div className="meta-stats">
          <div className="meta-stat">
            <div className="meta-stat__label">Realizado</div>
            <div className="meta-stat__value">
              {formatarMoeda(meta.valorRealizado)}
            </div>
          </div>
          <div className="meta-stat">
            <div className="meta-stat__label">Meta</div>
            <div className="meta-stat__value">
              {formatarMoeda(meta.valorEsperado)}
            </div>
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
        </div>

        {/* ── Tabela de medições ─────────────────────────────────────── */}
        <div className="meta-medicoes">
          <button
            type="button"
            className="meta-medicoes__toggle"
            onClick={() => setShowMedicoes((v) => !v)}
          >
            {showMedicoes ? '▾' : '▸'} Histórico de medições ({meta.medicoes.length})
          </button>

          {showMedicoes && (
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
                      <td>{m.loteDescricao}</td>
                      <td>
                        {formatarNumero(m.quantidadeLancada)}{' '}
                        {meta.tipoMeta === 'LEITE' ? 'L' : 'Kg'}
                      </td>
                      <td>
                        {formatarNumero(m.quantidadeConvertida)} {unidade}
                      </td>
                      <td>{m.criadoPorNome || m.criadoPorEmail || '—'}</td>
                      <td>
                        {podeEditarMedicao(m) && (
                          <button
                            type="button"
                            className="btn-edit-medicao"
                            onClick={() => setMedicaoEditando(m)}
                            aria-label="Editar medição"
                          >
                            ✎
                          </button>
                        )}
                        {podeEditarMedicao(m) && (
                          <button
                            type="button"
                            className="btn-del-medicao"
                            onClick={() => handleDeletarMedicao(m.id)}
                            disabled={deletandoMedicao === m.id}
                            aria-label="Remover medição"
                          >
                            {deletandoMedicao === m.id ? '...' : '✕'}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p className="meta-medicoes__empty">
                Nenhuma medição registrada ainda.
              </p>
            )
          )}
        </div>

        {/* ── Ações do card ──────────────────────────────────────────── */}
        <div className="meta-card__actions">
          <button
            type="button"
            className="btn-primary"
            onClick={() => setShowMedicaoModal(true)}
          >
            + Adicionar Medição
          </button>

          {podeGerenciar && (
            <>
              <button
                type="button"
                className="btn-secondary"
                onClick={() => onEditar(meta)}
              >
                Editar Meta
              </button>
              <button
                type="button"
                className="btn-danger"
                onClick={() => onDeletar(meta)}
              >
                Excluir
              </button>
            </>
          )}
        </div>
      </article>

      {/* ── Modal de nova medição ────────────────────────────────────── */}
      {showMedicaoModal && (
        <MedicaoModal
          meta={meta}
          lotes={lotes}
          emailUsuario={emailUsuario}
          onClose={() => setShowMedicaoModal(false)}
          onSaved={handleMedicaoSalva}
        />
      )}

      {/* ── Modal de edição de medição ───────────────────────────────── */}
      {medicaoEditando && (
        <MedicaoModal
          meta={meta}
          lotes={lotes}
          emailUsuario={emailUsuario}
          medicaoParaEditar={medicaoEditando}
          onClose={() => setMedicaoEditando(null)}
          onSaved={handleMedicaoSalva}
        />
      )}
    </>
  )
}

export default MetaCard
