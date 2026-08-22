import { useCallback, useEffect, useState } from 'react'
import {
  cancelarConsumoEstoque,
  editarConsumoEstoque,
  listarConsumoEstoque,
  registrarConsumoEstoque,
  resumoConsumoEstoquePorPeriodo,
} from '../integration/consumoEstoqueApi'
import CancelarConsumoEstoqueModal from './CancelarConsumoEstoqueModal'
import EditarConsumoEstoqueModal from './EditarConsumoEstoqueModal'

const defaultItem = { insumoId: '', quantidade: '', unidadeMedidaId: '' }
const defaultForm = { motivo: '', dataConsumo: '', itens: [{ ...defaultItem }] }

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

function ConsumoEstoqueTab({ currentUser, insumosEstoque }) {
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

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

  function handleFormChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  function insumoPorId(insumoId) {
    return insumosEstoque.find((i) => String(i.id) === String(insumoId)) ?? null
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

  async function handleSubmit(event) {
    event.preventDefault()
    setFeedback({ type: '', message: '' })
    setIsSaving(true)
    try {
      await registrarConsumoEstoque(currentUser.email, form)
      setFeedback({ type: 'info', message: 'Saída de estoque registrada com sucesso.' })
      setForm(defaultForm)
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
    <div className="insumos-split-layout">
      <div className="insumos-split-layout__form-card">
        <h2>Consumo de Estoque</h2>
        <p className="perfil-subtitle">
          Registra a saída de um ou mais produtos do próprio estoque, com o motivo/finalidade
          da retirada. Dá baixa automática no saldo dos produtos selecionados.
        </p>

        <form className="animal-form" onSubmit={handleSubmit}>
          <div className="consumo-estoque__itens">
            {form.itens.map((item, index) => {
              const insumoSelecionado = insumoPorId(item.insumoId)
              return (
                <div className="consumo-estoque__item-row" key={index}>
                  <label className="consumo-estoque__item-produto">
                    <span>
                      Produto <span className="required-marker" aria-hidden="true">*</span>
                    </span>
                    <select name="insumoId" value={item.insumoId} onChange={(e) => handleItemChange(index, e)} required>
                      <option value="">Selecione...</option>
                      {insumosEstoque.map((i) => (
                        <option key={i.id} value={i.id}>
                          {i.nome} (saldo: {i.saldoAtual} {i.unidadeMedidaPrimariaSigla})
                        </option>
                      ))}
                    </select>
                  </label>

                  <label className="consumo-estoque__item-quantidade">
                    <span>
                      Quantidade <span className="required-marker" aria-hidden="true">*</span>
                    </span>
                    <div className="consumo-estoque__quantidade-group">
                      <input
                        type="number"
                        name="quantidade"
                        value={item.quantidade}
                        onChange={(e) => handleItemChange(index, e)}
                        min="0.01"
                        step="0.01"
                        required
                      />
                      {insumoSelecionado?.unidadeMedidaSecundariaId ? (
                        <select
                          name="unidadeMedidaId"
                          className="consumo-estoque__unidade-inline"
                          value={item.unidadeMedidaId}
                          onChange={(e) => handleItemChange(index, e)}
                        >
                          <option value={insumoSelecionado.unidadeMedidaPrimariaId}>
                            {insumoSelecionado.unidadeMedidaPrimariaSigla}
                          </option>
                          <option value={insumoSelecionado.unidadeMedidaSecundariaId}>
                            {insumoSelecionado.unidadeMedidaSecundariaSigla}
                          </option>
                        </select>
                      ) : insumoSelecionado ? (
                        <span className="consumo-estoque__unidade-sufixo">
                          {insumoSelecionado.unidadeMedidaPrimariaSigla}
                        </span>
                      ) : null}
                    </div>
                  </label>

                  <button
                    type="button"
                    className="btn-icon btn-icon--danger consumo-estoque__remove-item"
                    onClick={() => removeItem(index)}
                    disabled={form.itens.length === 1}
                    aria-label="Remover produto"
                    title="Remover produto"
                  >
                    <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
                      <path
                        fill="currentColor"
                        d="M9 3a1 1 0 0 0-1 1v1H4a1 1 0 1 0 0 2h1v13a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7h1a1 1 0 1 0 0-2h-4V4a1 1 0 0 0-1-1H9zm1 2h4v0h-4v0zM7 7h10v13H7V7zm3 2a1 1 0 0 0-1 1v7a1 1 0 1 0 2 0v-7a1 1 0 0 0-1-1zm4 0a1 1 0 0 0-1 1v7a1 1 0 1 0 2 0v-7a1 1 0 0 0-1-1z"
                      />
                    </svg>
                  </button>
                </div>
              )
            })}

            <button type="button" className="btn-secondary consumo-estoque__add-item" onClick={addItem}>
              + Adicionar produto
            </button>
          </div>

          <label>
            <span>
              Motivo / finalidade da saída <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <textarea name="motivo" value={form.motivo} onChange={handleFormChange} rows={2} required />
          </label>

          <label>
            <span>Data do consumo</span>
            <input
              type="datetime-local"
              name="dataConsumo"
              value={form.dataConsumo}
              max={maxDataConsumo}
              onChange={handleFormChange}
            />
          </label>

          {feedback.message ? (
            <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
              {feedback.message}
            </p>
          ) : null}

          <div className="modal-actions">
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Registrando...' : 'Registrar saída'}
            </button>
          </div>
        </form>
      </div>

      <div className="insumos-split-layout__historico">
        <div className="insumos-historico__header">
          <h3>Log de movimentações</h3>
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
              ) : historico.length === 0 ? (
                <tr>
                  <td colSpan={6} className="table-empty">Nenhuma saída de estoque registrada.</td>
                </tr>
              ) : (
                historico.map((c) => (
                  <tr key={c.id}>
                    <td>{formatarData(c.dataConsumo)}</td>
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
                            {formatarData(c.canceladoEm)}
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
      </div>

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
          insumosEstoque={insumosEstoque}
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
              <button type="button" className="modal-close" onClick={() => setResumoAberto(false)}>
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
