import { useCallback, useEffect, useMemo, useState } from 'react'
import SearchSelectModal from '../../../components/shared/SearchSelectModal'
import { buscarAnimais } from '../../animais/integration/animalApi'
import { listarLotes } from '../../lotes/integration/loteApi'
import {
  listarVacinacoes,
  registrarVacinacao,
  editarVacinacao,
  cancelarVacinacao,
  resumoVacinacaoPorPeriodo,
} from '../integration/vacinacaoAnimalApi'
import RegistrarVacinacaoModal from './RegistrarVacinacaoModal'
import EditarVacinacaoAnimalModal from './EditarVacinacaoAnimalModal'
import CancelarVacinacaoAnimalModal from './CancelarVacinacaoAnimalModal'
import { formatarData, formatarDataHora } from '../../../utils/formatters'

const ANIMAL_COLUMNS = [
  { key: 'codigoBrinco', label: 'Código' },
  { key: 'racaNome', label: 'Raça' },
]

const STATUS_VACINAVEL = new Set(['ATIVO', 'OBSERVACAO'])

const defaultForm = {
  insumoId: '',
  quantidadePorAnimal: '',
  unidadeMedidaId: '',
  modoSelecao: 'ANIMAIS',
  loteId: '',
  animalIds: [],
  dataAplicacao: '',
}

const PERFIS_EDICAO_LIVRE = ['ADMINISTRADOR', 'GERENTE']

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

function VacinarAnimaisTab({ currentUser, insumosEstoque }) {
  const [animais, setAnimais] = useState([])
  const [lotes, setLotes] = useState([])
  const [registrarAberto, setRegistrarAberto] = useState(false)
  const [form, setForm] = useState(defaultForm)
  const [selecaoModalOpen, setSelecaoModalOpen] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [filtroInsumoId, setFiltroInsumoId] = useState('')
  const [historico, setHistorico] = useState([])
  const [isLoadingHistorico, setIsLoadingHistorico] = useState(false)
  const [filtroDataInicio, setFiltroDataInicio] = useState('')
  const [filtroDataFim, setFiltroDataFim] = useState('')

  const [editando, setEditando] = useState(null)
  const [editForm, setEditForm] = useState(null)
  const [editFeedback, setEditFeedback] = useState('')
  const [isSavingEdit, setIsSavingEdit] = useState(false)

  const [cancelando, setCancelando] = useState(null)
  const [justificativa, setJustificativa] = useState('')
  const [isCancelando, setIsCancelando] = useState(false)
  const [cancelamentoFeedback, setCancelamentoFeedback] = useState('')

  const [resumoAberto, setResumoAberto] = useState(false)
  const [resumo, setResumo] = useState([])
  const [isLoadingResumo, setIsLoadingResumo] = useState(false)
  const [resumoFeedback, setResumoFeedback] = useState('')

  const maxDataAplicacao = agoraDatetimeLocal()

  const vacinasDisponiveis = useMemo(
    () => insumosEstoque.filter((i) => i.tipo === 'VACINA'),
    [insumosEstoque],
  )

  const insumoSelecionado = useMemo(
    () => insumosEstoque.find((i) => String(i.id) === String(form.insumoId)) ?? null,
    [insumosEstoque, form.insumoId],
  )

  const animaisVacinaveis = useMemo(
    () => animais.filter((a) => STATUS_VACINAVEL.has(a.statusAnimal)),
    [animais],
  )

  useEffect(() => {
    buscarAnimais('')
      .then(setAnimais)
      .catch(() => setAnimais([]))
    listarLotes()
      .then(setLotes)
      .catch(() => setLotes([]))
  }, [])

  const fetchHistorico = useCallback(async (dataInicio, dataFim) => {
    setIsLoadingHistorico(true)
    try {
      const lista = await listarVacinacoes(dataInicio, dataFim)
      setHistorico(lista)
    } catch {
      setHistorico([])
    } finally {
      setIsLoadingHistorico(false)
    }
  }, [])

  useEffect(() => {
    fetchHistorico(filtroDataInicio, filtroDataFim)
  }, [filtroDataInicio, filtroDataFim, fetchHistorico])

  const historicoFiltrado = useMemo(() => {
    if (!filtroInsumoId) return historico
    return historico.filter((v) => String(v.insumoId) === String(filtroInsumoId))
  }, [historico, filtroInsumoId])

  function podeEditar(vacinacao) {
    if (vacinacao.cancelado) return false
    if (PERFIS_EDICAO_LIVRE.includes(currentUser?.perfil)) return true
    return vacinacao.criadoPorEmail === currentUser?.email
  }

  function podeCancelar(vacinacao) {
    if (vacinacao.cancelado) return false
    if (currentUser?.perfil === 'ADMINISTRADOR') return true
    return vacinacao.criadoPorEmail === currentUser?.email
  }

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => {
      const next = { ...current, [name]: value }
      if (name === 'insumoId') {
        next.unidadeMedidaId = ''
      }
      if (name === 'modoSelecao') {
        next.loteId = ''
        next.animalIds = []
      }
      return next
    })
  }

  function handleLoteSelecionado(event) {
    const loteId = event.target.value
    const lote = lotes.find((l) => String(l.id) === String(loteId))
    const idsDoLote = lote
      ? lote.alocacoes.flatMap((aloc) => aloc.animais.map((a) => a.id))
      : []
    const idsElegiveis = idsDoLote.filter((id) => animaisVacinaveis.some((a) => a.id === id))
    setForm((current) => ({ ...current, loteId, animalIds: idsElegiveis }))
  }

  function abrirRegistrar() {
    setForm(defaultForm)
    setFeedback({ type: '', message: '' })
    setRegistrarAberto(true)
  }

  function fecharRegistrar() {
    setRegistrarAberto(false)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFeedback({ type: '', message: '' })
    setIsSaving(true)
    try {
      const registrado = await registrarVacinacao(currentUser.email, form)
      setFeedback({
        type: 'info',
        message:
          `Aplicação registrada. Baixa total: ${registrado.quantidadeTotalBaixaUnidadePrimaria} ${registrado.unidadeMedidaPrimariaSigla}. ` +
          `Saldo restante: ${registrado.saldoAtualAposAplicacao} ${registrado.unidadeMedidaPrimariaSigla}. ` +
          `${registrado.totalAnimais} animal(is) cobertos.`,
      })
      setRegistrarAberto(false)
      await fetchHistorico(filtroDataInicio, filtroDataFim)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao registrar a aplicação.' })
    } finally {
      setIsSaving(false)
    }
  }

  function abrirEdicao(vacinacao) {
    setEditando(vacinacao)
    setEditFeedback('')
    setEditForm({
      quantidadePorAnimal: vacinacao.quantidadePorAnimal,
      unidadeMedidaId: '',
      dataAplicacao: vacinacao.dataAplicacao ? vacinacao.dataAplicacao.slice(0, 16) : '',
      maxDataAplicacao,
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
      await editarVacinacao(editando.id, currentUser.email, editForm)
      fecharEdicao()
      await fetchHistorico(filtroDataInicio, filtroDataFim)
    } catch (error) {
      setEditFeedback(error.message || 'Falha ao salvar a edição.')
    } finally {
      setIsSavingEdit(false)
    }
  }

  function abrirCancelamento(vacinacao) {
    setCancelando(vacinacao)
    setJustificativa('')
    setCancelamentoFeedback('')
  }

  function fecharCancelamento() {
    setCancelando(null)
    setJustificativa('')
    setCancelamentoFeedback('')
  }

  async function handleSubmitCancelamento(event) {
    event.preventDefault()
    setIsCancelando(true)
    setCancelamentoFeedback('')
    try {
      await cancelarVacinacao(cancelando.id, currentUser.email, justificativa)
      fecharCancelamento()
      await fetchHistorico(filtroDataInicio, filtroDataFim)
    } catch (error) {
      setCancelamentoFeedback(error.message || 'Falha ao cancelar a aplicação.')
    } finally {
      setIsCancelando(false)
    }
  }

  async function handleAbrirResumo() {
    setResumoAberto(true)
    setResumoFeedback('')
    const inicio = filtroDataInicio || hojeIso()
    const fim = filtroDataFim || hojeIso()
    setIsLoadingResumo(true)
    try {
      const lista = await resumoVacinacaoPorPeriodo(inicio, fim)
      setResumo(lista)
    } catch (error) {
      setResumoFeedback(error.message || 'Falha ao gerar o resumo.')
    } finally {
      setIsLoadingResumo(false)
    }
  }

  function renderAnimaisResumidos(animaisLista) {
    if (!animaisLista || animaisLista.length === 0) return '—'
    const maxExibidos = 3
    const codigos = animaisLista.slice(0, maxExibidos).map((a) => a.codigoBrinco).join(', ')
    const restante = animaisLista.length - maxExibidos
    return restante > 0 ? `${codigos} +${restante}` : codigos
  }

  const itensParaSelecao = form.modoSelecao === 'LOTE' && form.loteId
    ? animaisVacinaveis.filter((a) => {
        const lote = lotes.find((l) => String(l.id) === String(form.loteId))
        const idsDoLote = lote ? lote.alocacoes.flatMap((aloc) => aloc.animais.map((x) => x.id)) : []
        return idsDoLote.includes(a.id)
      })
    : animaisVacinaveis

  return (
    <div className="insumos-tab-content">
      <p className="perfil-subtitle">
        Aplicação de vacinas em animais individuais ou em um lote inteiro, com baixa automática
        no estoque (dose x nº de animais).
      </p>

      {feedback.message ? (
        <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
          {feedback.message}
        </p>
      ) : null}

      <div className="data-toolbar">
        <select
          className="toolbar-select"
          value={filtroInsumoId}
          onChange={(e) => setFiltroInsumoId(e.target.value)}
        >
          <option value="">Todas as vacinas</option>
          {vacinasDisponiveis.map((i) => (
            <option key={i.id} value={i.id}>
              {i.nome}
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
          + Registrar aplicação
        </button>
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Data</th>
              <th>Vacina</th>
              <th>Dose/animal</th>
              <th>Animais</th>
              <th>Lote</th>
              <th>Registrado por</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {isLoadingHistorico ? (
              <tr>
                <td colSpan={8} className="table-loading">Carregando...</td>
              </tr>
            ) : historicoFiltrado.length === 0 ? (
              <tr>
                <td colSpan={8} className="table-empty">Nenhuma aplicação registrada.</td>
              </tr>
            ) : (
              historicoFiltrado.map((v) => (
                <tr key={v.id}>
                  <td>{formatarDataHora(v.dataAplicacao)}</td>
                  <td>{v.insumoNome}</td>
                  <td>
                    {v.quantidadePorAnimal} {v.unidadeRegistroSigla}
                  </td>
                  <td>
                    {v.totalAnimais} ({renderAnimaisResumidos(v.animais)})
                  </td>
                  <td>{v.loteCodigo || '—'}</td>
                  <td>{v.criadoPorNome || v.criadoPorEmail || '—'}</td>
                  <td>
                    {v.cancelado ? (
                      <span className="consumo-estoque__status--cancelado">Cancelado</span>
                    ) : (
                      <span className="consumo-estoque__status--ativo">Ativo</span>
                    )}
                  </td>
                  <td>
                    <div className="row-actions">
                      {podeEditar(v) ? (
                        <button type="button" className="btn-row btn-row--edit" onClick={() => abrirEdicao(v)}>
                          Editar
                        </button>
                      ) : null}
                      {podeCancelar(v) ? (
                        <button type="button" className="btn-row btn-row--danger" onClick={() => abrirCancelamento(v)}>
                          Cancelar
                        </button>
                      ) : null}
                      {!podeEditar(v) && !podeCancelar(v) ? <span>—</span> : null}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {registrarAberto ? (
        <RegistrarVacinacaoModal
          formData={form}
          vacinasDisponiveis={vacinasDisponiveis}
          insumoSelecionado={insumoSelecionado}
          lotes={lotes}
          maxDataAplicacao={maxDataAplicacao}
          isSaving={isSaving}
          feedback={feedback.type === 'error' ? feedback.message : ''}
          onClose={fecharRegistrar}
          onChange={handleChange}
          onLoteSelecionado={handleLoteSelecionado}
          onAbrirSelecaoAnimais={() => setSelecaoModalOpen(true)}
          onSubmit={handleSubmit}
        />
      ) : null}

      {selecaoModalOpen ? (
        <SearchSelectModal
          title="Selecionar animais"
          items={itensParaSelecao}
          selectedIds={form.animalIds}
          onConfirm={(ids) => {
            setForm((current) => ({ ...current, animalIds: ids }))
            setSelecaoModalOpen(false)
          }}
          onClose={() => setSelecaoModalOpen(false)}
          multiSelect
          columns={ANIMAL_COLUMNS}
        />
      ) : null}

      {editando && editForm ? (
        <EditarVacinacaoAnimalModal
          vacinacao={editando}
          formData={editForm}
          isSaving={isSavingEdit}
          feedback={editFeedback}
          onClose={fecharEdicao}
          onChange={handleEditChange}
          onSubmit={handleSubmitEdicao}
        />
      ) : null}

      {cancelando ? (
        <CancelarVacinacaoAnimalModal
          vacinacao={cancelando}
          justificativa={justificativa}
          isSaving={isCancelando}
          feedback={cancelamentoFeedback}
          onClose={fecharCancelamento}
          onChange={(e) => setJustificativa(e.target.value)}
          onSubmit={handleSubmitCancelamento}
        />
      ) : null}

      {resumoAberto ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal-card">
            <div className="modal-header">
              <h2>Resumo de vacinação</h2>
              <button type="button" className="modal-close" aria-label="Fechar" onClick={() => setResumoAberto(false)}>
                ✕
              </button>
            </div>

            <p className="form-help">
              Período: {formatarData(filtroDataInicio || hojeIso())} a {formatarData(filtroDataFim || hojeIso())}
            </p>

            {resumoFeedback ? <p className="feedback feedback--error">{resumoFeedback}</p> : null}

            {isLoadingResumo ? (
              <p>Carregando...</p>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Produto</th>
                    <th>Quantidade total aplicada</th>
                  </tr>
                </thead>
                <tbody>
                  {resumo.length === 0 ? (
                    <tr>
                      <td colSpan={2} className="table-empty">Nenhuma aplicação no período.</td>
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

export default VacinarAnimaisTab
