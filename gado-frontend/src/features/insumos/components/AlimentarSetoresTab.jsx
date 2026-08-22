import { useCallback, useEffect, useMemo, useState } from 'react'
import { listarSetores } from '../../setores/integration/setorApi'
import {
  listarConsumoPorSetor,
  registrarConsumo,
  editarConsumo,
  resumoPorSetorEPeriodo,
} from '../integration/consumoInsumoApi'
import EditarConsumoInsumoModal from './EditarConsumoInsumoModal'

const defaultForm = {
  setorId: '',
  insumoId: '',
  quantidade: '',
  unidadeMedidaId: '',
  dataConsumo: '',
}

const PERFIS_EDICAO_LIVRE = ['ADMINISTRADOR', 'GERENTE']

function formatarData(iso) {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return iso
  return data.toLocaleString('pt-BR')
}

function agoraDatetimeLocal() {
  const now = new Date()
  now.setSeconds(0, 0)
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
  return now.toISOString().slice(0, 16)
}

function hojeIso() {
  const now = new Date()
  const yyyy = now.getFullYear()
  const mm = String(now.getMonth() + 1).padStart(2, '0')
  const dd = String(now.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function AlimentarSetoresTab({ currentUser, insumosEstoque }) {
  const [setores, setSetores] = useState([])
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [resultado, setResultado] = useState(null)
  const [historico, setHistorico] = useState([])
  const [isLoadingHistorico, setIsLoadingHistorico] = useState(false)
  const [filtroDataInicio, setFiltroDataInicio] = useState('')
  const [filtroDataFim, setFiltroDataFim] = useState('')
  const [editando, setEditando] = useState(null)
  const [editForm, setEditForm] = useState(null)
  const [editFeedback, setEditFeedback] = useState('')
  const [isSavingEdit, setIsSavingEdit] = useState(false)
  const [resumoAberto, setResumoAberto] = useState(false)
  const [resumo, setResumo] = useState([])
  const [isLoadingResumo, setIsLoadingResumo] = useState(false)
  const [resumoFeedback, setResumoFeedback] = useState('')

  const maxDataConsumo = agoraDatetimeLocal()

  useEffect(() => {
    listarSetores()
      .then(setSetores)
      .catch(() => setSetores([]))
  }, [])

  const insumoSelecionado = useMemo(
    () => insumosEstoque.find((i) => String(i.id) === String(form.insumoId)) ?? null,
    [insumosEstoque, form.insumoId],
  )

  const fetchHistorico = useCallback(async (setorId, dataInicio, dataFim) => {
    if (!setorId) {
      setHistorico([])
      return
    }
    setIsLoadingHistorico(true)
    try {
      const lista = await listarConsumoPorSetor(setorId, dataInicio, dataFim)
      setHistorico(lista)
    } catch {
      setHistorico([])
    } finally {
      setIsLoadingHistorico(false)
    }
  }, [])

  useEffect(() => {
    fetchHistorico(form.setorId, filtroDataInicio, filtroDataFim)
  }, [form.setorId, filtroDataInicio, filtroDataFim, fetchHistorico])

  function podeEditar(consumo) {
    if (PERFIS_EDICAO_LIVRE.includes(currentUser?.perfil)) return true
    return consumo.registradoPorEmail === currentUser?.email
  }

  function abrirEdicao(consumo) {
    setEditando(consumo)
    setEditFeedback('')
    setEditForm({
      quantidade: consumo.quantidadeRegistrada,
      unidadeMedidaId: '',
      dataConsumo: consumo.dataConsumo ? consumo.dataConsumo.slice(0, 16) : '',
      maxDataConsumo,
    })
  }

  function fecharEdicao() {
    setEditando(null)
    setEditForm(null)
    setEditFeedback('')
  }

  function handleEditChange(event) {
    const { name, value } = event.target
    setEditForm((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmitEdicao(event) {
    event.preventDefault()
    setIsSavingEdit(true)
    setEditFeedback('')
    try {
      await editarConsumo(editando.id, currentUser.email, editForm)
      fecharEdicao()
      await fetchHistorico(form.setorId, filtroDataInicio, filtroDataFim)
    } catch (error) {
      setEditFeedback(error.message || 'Falha ao salvar a edição.')
    } finally {
      setIsSavingEdit(false)
    }
  }

  async function handleAbrirResumo() {
    setResumoAberto(true)
    setResumoFeedback('')
    if (!form.setorId) {
      setResumoFeedback('Selecione um setor para gerar o resumo.')
      return
    }
    const inicio = filtroDataInicio || hojeIso()
    const fim = filtroDataFim || hojeIso()
    setIsLoadingResumo(true)
    try {
      const lista = await resumoPorSetorEPeriodo(form.setorId, inicio, fim)
      setResumo(lista)
    } catch (error) {
      setResumoFeedback(error.message || 'Falha ao gerar o resumo.')
    } finally {
      setIsLoadingResumo(false)
    }
  }

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => {
      const next = { ...current, [name]: value }
      if (name === 'insumoId') {
        next.unidadeMedidaId = '' // volta para a unidade primária ao trocar de insumo
      }
      return next
    })
    setResultado(null)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFeedback({ type: '', message: '' })
    setResultado(null)
    setIsSaving(true)
    try {
      const registrado = await registrarConsumo(currentUser.email, form)
      setResultado(registrado)
      setFeedback({ type: 'info', message: 'Consumo registrado com sucesso.' })
      setForm((current) => ({
        ...defaultForm,
        setorId: current.setorId, // mantém o setor selecionado para facilitar novos lançamentos
      }))
      await fetchHistorico(form.setorId)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao registrar o consumo.' })
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="insumos-split-layout">
      <div className="insumos-split-layout__form-card">
        <h2>Alimentar Setores</h2>
        <p className="perfil-subtitle">
          Registra o consumo de um insumo em um Setor, dá baixa automática no estoque e
          calcula o rateio entre os animais alocados naquele setor.
        </p>

        <form className="animal-form" onSubmit={handleSubmit}>
          <label>
            <span>
              Setor <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="setorId" value={form.setorId} onChange={handleChange} required>
              <option value="">Selecione o setor...</option>
              {setores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nome}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Insumo <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="insumoId" value={form.insumoId} onChange={handleChange} required>
              <option value="">Selecione o insumo...</option>
              {insumosEstoque.map((i) => (
                <option key={i.id} value={i.id}>
                  {i.nome} (saldo: {i.saldoAtual} {i.unidadeMedidaPrimariaSigla})
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Quantidade consumida <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="number"
              name="quantidade"
              value={form.quantidade}
              onChange={handleChange}
              min="0.01"
              step="0.01"
              required
            />
          </label>

          {insumoSelecionado?.unidadeMedidaSecundariaId ? (
            <label>
              <span>Unidade informada</span>
              <select name="unidadeMedidaId" value={form.unidadeMedidaId} onChange={handleChange}>
                <option value={insumoSelecionado.unidadeMedidaPrimariaId}>
                  {insumoSelecionado.unidadeMedidaPrimariaSigla} (unidade de estoque)
                </option>
                <option value={insumoSelecionado.unidadeMedidaSecundariaId}>
                  {insumoSelecionado.unidadeMedidaSecundariaSigla}
                </option>
              </select>
            </label>
          ) : insumoSelecionado ? (
            <p className="form-help">
              Unidade: {insumoSelecionado.unidadeMedidaPrimariaSigla}
            </p>
          ) : null}

          <label>
            <span>Data do consumo</span>
            <input
              type="datetime-local"
              name="dataConsumo"
              value={form.dataConsumo}
              max={maxDataConsumo}
              onChange={handleChange}
            />
          </label>

          {feedback.message ? (
            <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
              {feedback.message}
            </p>
          ) : null}

          <div className="modal-actions">
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Registrando...' : 'Registrar consumo'}
            </button>
          </div>
        </form>

        {resultado ? (
          <dl className="details-grid insumos-split-layout__resultado">
            <div>
              <dt>Baixa no estoque</dt>
              <dd>
                {resultado.quantidadeBaixaUnidadePrimaria} {resultado.unidadeMedidaPrimariaSigla}
              </dd>
            </div>
            <div>
              <dt>Saldo restante</dt>
              <dd>
                {resultado.saldoAtualAposConsumo} {resultado.unidadeMedidaPrimariaSigla}
              </dd>
            </div>
            <div>
              <dt>Animais no setor</dt>
              <dd>{resultado.totalAnimaisSetor}</dd>
            </div>
            <div>
              <dt>Consumo por animal</dt>
              <dd>
                {resultado.consumoPorAnimal != null
                  ? `${resultado.consumoPorAnimal.toFixed(3)} ${resultado.unidadeRegistroSigla}`
                  : 'sem animais alocados'}
              </dd>
            </div>
          </dl>
        ) : null}
      </div>

      <div className="insumos-split-layout__historico">
        <div className="insumos-historico__header">
          <h3>Histórico do setor</h3>
          <button type="button" className="btn-row" onClick={handleAbrirResumo}>
            Resumo
          </button>
        </div>

        <div className="data-toolbar">
          <label className="toolbar-date-label">
            <span className="toolbar-date-label__text">De</span>
            <input
              type="date"
              className="toolbar-date"
              value={filtroDataInicio}
              max={filtroDataFim || undefined}
              onChange={(e) => setFiltroDataInicio(e.target.value)}
            />
          </label>
          <label className="toolbar-date-label">
            <span className="toolbar-date-label__text">Até</span>
            <input
              type="date"
              className="toolbar-date"
              value={filtroDataFim}
              min={filtroDataInicio || undefined}
              onChange={(e) => setFiltroDataFim(e.target.value)}
            />
          </label>
        </div>

        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Data</th>
                <th>Insumo</th>
                <th>Quantidade</th>
                <th>Animais</th>
                <th>Por animal</th>
                <th>Registrado por</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {!form.setorId ? (
                <tr>
                  <td colSpan={7} className="table-empty">Selecione um setor para ver o histórico.</td>
                </tr>
              ) : isLoadingHistorico ? (
                <tr>
                  <td colSpan={7} className="table-loading">Carregando...</td>
                </tr>
              ) : historico.length === 0 ? (
                <tr>
                  <td colSpan={7} className="table-empty">Nenhum consumo registrado neste setor.</td>
                </tr>
              ) : (
                historico.map((c) => (
                  <tr key={c.id}>
                    <td>{formatarData(c.dataConsumo)}</td>
                    <td>{c.insumoNome}</td>
                    <td>
                      {c.quantidadeRegistrada} {c.unidadeRegistroSigla}
                    </td>
                    <td>{c.totalAnimaisSetor}</td>
                    <td>
                      {c.consumoPorAnimal != null
                        ? `${c.consumoPorAnimal.toFixed(3)} ${c.unidadeRegistroSigla}`
                        : '—'}
                    </td>
                    <td>{c.registradoPorNome || c.registradoPorEmail || '—'}</td>
                    <td>
                      {podeEditar(c) ? (
                        <button type="button" className="btn-row btn-row--edit" onClick={() => abrirEdicao(c)}>
                          Editar
                        </button>
                      ) : (
                        '—'
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {editando && editForm ? (
        <EditarConsumoInsumoModal
          consumo={editando}
          formData={editForm}
          isSaving={isSavingEdit}
          feedback={editFeedback}
          onClose={fecharEdicao}
          onChange={handleEditChange}
          onSubmit={handleSubmitEdicao}
        />
      ) : null}

      {resumoAberto ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal-card">
            <div className="modal-header">
              <h2>Resumo de consumo por setor</h2>
              <button type="button" className="modal-close" onClick={() => setResumoAberto(false)}>
                ✕
              </button>
            </div>

            <p className="form-help">
              Setor: {setores.find((s) => String(s.id) === String(form.setorId))?.nome ?? '—'} · Período:{' '}
              {formatarData(filtroDataInicio || hojeIso())} a {formatarData(filtroDataFim || hojeIso())}
            </p>

            {resumoFeedback ? <p className="feedback feedback--error">{resumoFeedback}</p> : null}

            {isLoadingResumo ? (
              <p>Carregando...</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Produto</th>
                    <th>Quantidade total consumida</th>
                  </tr>
                </thead>
                <tbody>
                  {resumo.length === 0 ? (
                    <tr>
                      <td colSpan={2} className="table-empty">Nenhum consumo no período.</td>
                    </tr>
                  ) : (
                    resumo.map((item) => (
                      <tr key={item.insumoId}>
                        <td>{item.insumoNome}</td>
                        <td>
                          {item.quantidadeTotal.toFixed(2)} {item.unidadeSigla}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            )}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={() => setResumoAberto(false)}>
                Fechar
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  )
}

export default AlimentarSetoresTab
