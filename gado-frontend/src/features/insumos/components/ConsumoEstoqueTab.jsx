import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  cancelarConsumoEstoque,
  editarConsumoEstoque,
  listarConsumoEstoque,
  registrarConsumoEstoque,
  resumoConsumoEstoquePorPeriodo,
} from '../integration/consumoEstoqueApi'
import RegistrarConsumoEstoqueModal from './RegistrarConsumoEstoqueModal'
import CancelarConsumoEstoqueModal from './CancelarConsumoEstoqueModal'
import EditarConsumoEstoqueModal from './EditarConsumoEstoqueModal'
import { formatarData, formatarDataHora } from '../../../utils/formatters'

const defaultItem = { insumoId: '', quantidade: '', unidadeMedidaId: '' }
const defaultForm = { motivo: '', dataConsumo: '', itens: [{ ...defaultItem }] }

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

function ConsumoEstoqueTab({ currentUser, insumosEstoque }) {
  const [registrarAberto, setRegistrarAberto] = useState(false)
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [filtroInsumoId, setFiltroInsumoId] = useState('')
  const [historico, setHistorico] = useState([])
  const [isLoadingHistorico, setIsLoadingHistorico] = useState(false)
  const [filtroDataInicio, setFiltroDataInicio] = useState('')
  const [filtroDataFim, setFiltroDataFim] = useState('')

  const [consumoParaCancelar, setConsumoParaCancelar] = useState(null)
  const [justificativa, setJustificativa] = useState('')
  const [isCancelando, setIsCancelando] = useState(false)
  const [cancelamentoFeedback, setCancelamentoFeedback] = useState('')

  const [editando, setEditando] = useState(null)
  const [editForm, setEditForm] = useState(null)
  const [editFeedback, setEditFeedback] = useState('')
  const [isSavingEdit, setIsSavingEdit] = useState(false)

  const [resumoAberto, setResumoAberto] = useState(false)
  const [resumo, setResumo] = useState([])
  const [isLoadingResumo, setIsLoadingResumo] = useState(false)
  const [resumoFeedback, setResumoFeedback] = useState('')

  const maxDataConsumo = agoraDatetimeLocal()

  const insumosConsumiveis = useMemo(
    () => insumosEstoque.filter((i) => !TIPOS_NAO_CONSUMIVEIS.has(i.tipo)),
    [insumosEstoque],
  )

  const podeCancelar = useCallback(
    (consumo) =>
      !consumo.cancelado &&
      (currentUser?.perfil === 'ADMINISTRADOR' ||
        (currentUser?.email &&
          consumo.criadoPorEmail &&
          currentUser.email.trim().toLowerCase() === consumo.criadoPorEmail.trim().toLowerCase())),
    [currentUser],
  )

  const podeEditar = useCallback(
    (consumo) => {
      if (consumo.cancelado) return false
      if (PERFIS_EDICAO_LIVRE.includes(currentUser?.perfil)) return true
      return (
        currentUser?.email &&
        consumo.criadoPorEmail &&
        currentUser.email.trim().toLowerCase() === consumo.criadoPorEmail.trim().toLowerCase()
      )
    },
    [currentUser],
  )

  const fetchHistorico = useCallback(async (dataInicio, dataFim) => {
    setIsLoadingHistorico(true)
    try {
      const lista = await listarConsumoEstoque(dataInicio, dataFim)
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
    return historico.filter((c) => c.itens.some((item) => String(item.insumoId) === String(filtroInsumoId)))
  }, [historico, filtroInsumoId])

  function handleFormChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  function handleItemChange(index, event) {
    const { name, value } = event.target
    setForm((current) => {
      const itens = current.itens.map((item, i) => {
        if (i !== index) return item
        const nextItem = { ...item, [name]: value }
        if (name === 'insumoId') {
          nextItem.unidadeMedidaId = ''
        }
        return nextItem
      })
      return { ...current, itens }
    })
  }

  function addItem() {
    setForm((current) => ({ ...current, itens: [...current.itens, { ...defaultItem }] }))
  }

  function removeItem(index) {
    setForm((current) => ({
      ...current,
      itens: current.itens.length > 1 ? current.itens.filter((_, i) => i !== index) : current.itens,
    }))
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
      await registrarConsumoEstoque(currentUser.email, form)
      setFeedback({ type: 'info', message: 'Saída de estoque registrada com sucesso.' })
      setRegistrarAberto(false)
      await fetchHistorico(filtroDataInicio, filtroDataFim)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao registrar a saída de estoque.' })
    } finally {
      setIsSaving(false)
    }
  }

  function abrirCancelamento(consumo) {
    setConsumoParaCancelar(consumo)
    setJustificativa('')
    setCancelamentoFeedback('')
  }

  function fecharCancelamento() {
    setConsumoParaCancelar(null)
    setJustificativa('')
    setCancelamentoFeedback('')
  }

  async function handleSubmitCancelamento(event) {
    event.preventDefault()
    setIsCancelando(true)
    setCancelamentoFeedback('')
    try {
      await cancelarConsumoEstoque(consumoParaCancelar.id, currentUser.email, justificativa)
      fecharCancelamento()
      await fetchHistorico(filtroDataInicio, filtroDataFim)
    } catch (error) {
      setCancelamentoFeedback(error.message || 'Falha ao cancelar a movimentação.')
    } finally {
      setIsCancelando(false)
    }
  }

  function abrirEdicao(consumo) {
    setEditando(consumo)
    setEditFeedback('')
    setEditForm({
      motivo: consumo.motivo,
      dataConsumo: consumo.dataConsumo ? consumo.dataConsumo.slice(0, 16) : '',
      maxDataConsumo,
      itens: consumo.itens.map((item) => ({
        insumoId: item.insumoId,
        quantidade: item.quantidadeRegistrada,
        unidadeMedidaId: '',
      })),
    })
  }

  function fecharEdicao() {
    setEditando(null)
    setEditForm(null)
    setEditFeedback('')
  }

  function handleEditFormChange(event) {
    const { name, value } = event.target
    setEditForm((current) => ({ ...current, [name]: value }))
  }

  function handleEditItemChange(index, event) {
    const { name, value } = event.target
    setEditForm((current) => {
      const itens = current.itens.map((item, i) => {
        if (i !== index) return item
        const nextItem = { ...item, [name]: value }
        if (name === 'insumoId') nextItem.unidadeMedidaId = ''
        return nextItem
      })
      return { ...current, itens }
    })
  }

  function addEditItem() {
    setEditForm((current) => ({ ...current, itens: [...current.itens, { ...defaultItem }] }))
  }

  function removeEditItem(index) {
    setEditForm((current) => ({
      ...current,
      itens: current.itens.length > 1 ? current.itens.filter((_, i) => i !== index) : current.itens,
    }))
  }

  async function handleSubmitEdicao(event) {
    event.preventDefault()
    setIsSavingEdit(true)
    setEditFeedback('')
    try {
      await editarConsumoEstoque(editando.id, currentUser.email, editForm)
      fecharEdicao()
      await fetchHistorico(filtroDataInicio, filtroDataFim)
    } catch (error) {
      setEditFeedback(error.message || 'Falha ao salvar a edição.')
    } finally {
      setIsSavingEdit(false)
    }
  }

  async function handleAbrirResumo() {
    setResumoAberto(true)
    setResumoFeedback('')
    const inicio = filtroDataInicio || hojeIso()
    const fim = filtroDataFim || hojeIso()
    setIsLoadingResumo(true)
    try {
      const lista = await resumoConsumoEstoquePorPeriodo(inicio, fim)
      setResumo(lista)
    } catch (error) {
      setResumoFeedback(error.message || 'Falha ao gerar o resumo.')
    } finally {
      setIsLoadingResumo(false)
    }
  }

  return (
    <div className="insumos-tab-content">
      <p className="perfil-subtitle">
        Saídas de estoque com motivo/finalidade da retirada e baixa automática no saldo dos
        produtos selecionados.
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
          <option value="">Todos os produtos</option>
          {insumosConsumiveis.map((i) => (
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
          + Registrar saída
        </button>
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Data</th>
              <th>Produtos</th>
              <th>Motivo</th>
              <th>Registrado por</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {isLoadingHistorico ? (
              <tr>
                <td colSpan={6} className="table-loading">Carregando...</td>
              </tr>
            ) : historicoFiltrado.length === 0 ? (
              <tr>
                <td colSpan={6} className="table-empty">Nenhuma saída de estoque registrada.</td>
              </tr>
            ) : (
              historicoFiltrado.map((c) => (
                <tr key={c.id}>
                  <td>{formatarDataHora(c.dataConsumo)}</td>
                  <td>
                    {c.itens.map((item) => (
                      <div key={item.id}>
                        {item.insumoNome}: {item.quantidadeRegistrada} {item.unidadeRegistroSigla}
                      </div>
                    ))}
                  </td>
                  <td>{c.motivo}</td>
                  <td>{c.criadoPorNome || c.criadoPorEmail || '—'}</td>
                  <td>
                    {c.cancelado ? (
                      <>
                        <span className="consumo-estoque__status--cancelado">Cancelado</span>
                        <span className="consumo-estoque__cancelamento-motivo">
                          {c.motivoCancelamento} — {c.canceladoPorNome || c.canceladoPorEmail} em{' '}
                          {formatarDataHora(c.canceladoEm)}
                        </span>
                      </>
                    ) : (
                      <span className="consumo-estoque__status--ativo">Ativo</span>
                    )}
                  </td>
                  <td>
                    <div className="row-actions">
                      {podeEditar(c) ? (
                        <button type="button" className="btn-row btn-row--edit" onClick={() => abrirEdicao(c)}>
                          Editar
                        </button>
                      ) : null}
                      {podeCancelar(c) ? (
                        <button type="button" className="btn-row btn-row--danger" onClick={() => abrirCancelamento(c)}>
                          Cancelar
                        </button>
                      ) : null}
                      {!podeEditar(c) && !podeCancelar(c) ? <span>—</span> : null}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {registrarAberto ? (
        <RegistrarConsumoEstoqueModal
          formData={form}
          insumosEstoque={insumosConsumiveis}
          maxDataConsumo={maxDataConsumo}
          isSaving={isSaving}
          feedback={feedback.type === 'error' ? feedback.message : ''}
          onClose={fecharRegistrar}
          onChange={handleFormChange}
          onItemChange={handleItemChange}
          onAddItem={addItem}
          onRemoveItem={removeItem}
          onSubmit={handleSubmit}
        />
      ) : null}

      {consumoParaCancelar ? (
        <CancelarConsumoEstoqueModal
          consumo={consumoParaCancelar}
          justificativa={justificativa}
          isSaving={isCancelando}
          feedback={cancelamentoFeedback}
          onClose={fecharCancelamento}
          onChange={(e) => setJustificativa(e.target.value)}
          onSubmit={handleSubmitCancelamento}
        />
      ) : null}

      {editando && editForm ? (
        <EditarConsumoEstoqueModal
          consumo={editando}
          formData={editForm}
          insumosEstoque={insumosConsumiveis}
          isSaving={isSavingEdit}
          feedback={editFeedback}
          onClose={fecharEdicao}
          onChange={handleEditFormChange}
          onItemChange={handleEditItemChange}
          onAddItem={addEditItem}
          onRemoveItem={removeEditItem}
          onSubmit={handleSubmitEdicao}
        />
      ) : null}

      {resumoAberto ? (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal-card">
            <div className="modal-header">
              <h2>Resumo de consumo de estoque</h2>
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

export default ConsumoEstoqueTab
