import { useCallback, useEffect, useMemo, useState } from 'react'
import { listarSetores } from '../../setores/integration/setorApi'
import {
  listarConsumoPorSetor,
  registrarConsumo,
  editarConsumo,
  resumoPorSetorEPeriodo,
} from '../integration/consumoInsumoApi'
import RegistrarConsumoInsumoModal from './RegistrarConsumoInsumoModal'
import EditarConsumoInsumoModal from './EditarConsumoInsumoModal'
import { formatarData, formatarDataHora } from '../../../utils/formatters'

const defaultForm = {
  setorId: '',
  insumoId: '',
  quantidade: '',
  unidadeMedidaId: '',
  dataConsumo: '',
}

const PERFIS_EDICAO_LIVRE = ['ADMINISTRADOR', 'GERENTE']

// Cabeças de animal só saem por venda no Financeiro, e vacinas só pela página "Vacinar Animais".
const TIPOS_NAO_CONSUMIVEIS = new Set(['ANIMAL', 'VACINA'])

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
  const [filtroSetorId, setFiltroSetorId] = useState('')
  const [registrarAberto, setRegistrarAberto] = useState(false)
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
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

  const insumosConsumiveis = useMemo(
    () => insumosEstoque.filter((i) => !TIPOS_NAO_CONSUMIVEIS.has(i.tipo)),
    [insumosEstoque],
  )

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
    fetchHistorico(filtroSetorId, filtroDataInicio, filtroDataFim)
  }, [filtroSetorId, filtroDataInicio, filtroDataFim, fetchHistorico])

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
      await fetchHistorico(filtroSetorId, filtroDataInicio, filtroDataFim)
    } catch (error) {
      setEditFeedback(error.message || 'Falha ao salvar a edição.')
    } finally {
      setIsSavingEdit(false)
    }
  }

  async function handleAbrirResumo() {
    setResumoAberto(true)
    setResumoFeedback('')
    if (!filtroSetorId) {
      setResumoFeedback('Selecione um setor para gerar o resumo.')
      return
    }
    const inicio = filtroDataInicio || hojeIso()
    const fim = filtroDataFim || hojeIso()
    setIsLoadingResumo(true)
    try {
      const lista = await resumoPorSetorEPeriodo(filtroSetorId, inicio, fim)
      setResumo(lista)
    } catch (error) {
      setResumoFeedback(error.message || 'Falha ao gerar o resumo.')
    } finally {
      setIsLoadingResumo(false)
    }
  }

  function abrirRegistrar() {
    setForm({ ...defaultForm, setorId: filtroSetorId || '' })
    setFeedback({ type: '', message: '' })
    setRegistrarAberto(true)
  }

  function fecharRegistrar() {
    setRegistrarAberto(false)
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
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFeedback({ type: '', message: '' })
    setIsSaving(true)
    try {
      const registrado = await registrarConsumo(currentUser.email, form)
      const porAnimalTexto =
        registrado.consumoPorAnimal != null
          ? `, ${registrado.consumoPorAnimal.toFixed(3)} ${registrado.unidadeRegistroSigla} por animal`
          : ''
      setFeedback({
        type: 'info',
        message:
          `Consumo registrado. Baixa: ${registrado.quantidadeBaixaUnidadePrimaria} ${registrado.unidadeMedidaPrimariaSigla}. ` +
          `Saldo restante: ${registrado.saldoAtualAposConsumo} ${registrado.unidadeMedidaPrimariaSigla}. ` +
          `${registrado.totalAnimaisSetor} animal(is) no setor${porAnimalTexto}.`,
      })
      setFiltroSetorId(form.setorId)
      setRegistrarAberto(false)
      await fetchHistorico(form.setorId, filtroDataInicio, filtroDataFim)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao registrar o consumo.' })
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="insumos-tab-content">
      <p className="perfil-subtitle">
        Consumo de insumos por setor: dá baixa automática no estoque e calcula o rateio entre
        os animais alocados em cada setor.
      </p>

      {feedback.message ? (
        <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
          {feedback.message}
        </p>
      ) : null}

      <div className="data-toolbar">
        <select
          className="toolbar-select"
          value={filtroSetorId}
          onChange={(e) => setFiltroSetorId(e.target.value)}
        >
          <option value="">Selecione o setor...</option>
          {setores.map((s) => (
            <option key={s.id} value={s.id}>
              {s.nome}
            </option>
          ))}
        </select>

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

        <button type="button" className="btn-row" onClick={handleAbrirResumo}>
          Resumo
        </button>
        <button type="button" className="btn-new-entity" onClick={abrirRegistrar}>
          + Registrar consumo
        </button>
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
            {!filtroSetorId ? (
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
                  <td>{formatarDataHora(c.dataConsumo)}</td>
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

      {registrarAberto ? (
        <RegistrarConsumoInsumoModal
          formData={form}
          setores={setores}
          insumosEstoque={insumosConsumiveis}
          insumoSelecionado={insumoSelecionado}
          maxDataConsumo={maxDataConsumo}
          isSaving={isSaving}
          feedback={feedback.type === 'error' ? feedback.message : ''}
          onClose={fecharRegistrar}
          onChange={handleChange}
          onSubmit={handleSubmit}
        />
      ) : null}

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
              <button type="button" className="modal-close" aria-label="Fechar" onClick={() => setResumoAberto(false)}>
                ✕
              </button>
            </div>

            <p className="form-help">
              Setor: {setores.find((s) => String(s.id) === String(filtroSetorId))?.nome ?? '—'} · Período:{' '}
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
