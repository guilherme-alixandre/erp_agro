import { useCallback, useEffect, useState } from 'react'
import {
  cancelarConsumoEstoque,
  listarConsumoEstoque,
  registrarConsumoEstoque,
} from '../integration/consumoEstoqueApi'
import CancelarConsumoEstoqueModal from './CancelarConsumoEstoqueModal'

const defaultItem = { insumoId: '', quantidade: '', unidadeMedidaId: '' }
const defaultForm = { motivo: '', dataConsumo: '', itens: [{ ...defaultItem }] }

function formatarData(iso) {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return iso
  return data.toLocaleString('pt-BR')
}

function ConsumoEstoqueTab({ currentUser, insumosEstoque }) {
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [historico, setHistorico] = useState([])
  const [isLoadingHistorico, setIsLoadingHistorico] = useState(false)

  const [consumoParaCancelar, setConsumoParaCancelar] = useState(null)
  const [justificativa, setJustificativa] = useState('')
  const [isCancelando, setIsCancelando] = useState(false)
  const [cancelamentoFeedback, setCancelamentoFeedback] = useState('')

  const podeCancelar = useCallback(
    (consumo) =>
      !consumo.cancelado &&
      (currentUser?.perfil === 'ADMINISTRADOR' ||
        (currentUser?.email &&
          consumo.criadoPorEmail &&
          currentUser.email.trim().toLowerCase() === consumo.criadoPorEmail.trim().toLowerCase())),
    [currentUser],
  )

  const fetchHistorico = useCallback(async () => {
    setIsLoadingHistorico(true)
    try {
      const lista = await listarConsumoEstoque()
      setHistorico(lista)
    } catch {
      setHistorico([])
    } finally {
      setIsLoadingHistorico(false)
    }
  }, [])

  useEffect(() => {
    fetchHistorico()
  }, [fetchHistorico])

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
      await fetchHistorico()
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
      await fetchHistorico()
    } catch (error) {
      setCancelamentoFeedback(error.message || 'Falha ao cancelar a movimentação.')
    } finally {
      setIsCancelando(false)
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
                  <label>
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

                  <label>
                    <span>
                      Quantidade <span className="required-marker" aria-hidden="true">*</span>
                    </span>
                    <input
                      type="number"
                      name="quantidade"
                      value={item.quantidade}
                      onChange={(e) => handleItemChange(index, e)}
                      min="0.01"
                      step="0.01"
                      required
                    />
                  </label>

                  {insumoSelecionado?.unidadeMedidaSecundariaId ? (
                    <label>
                      <span>Unidade</span>
                      <select
                        name="unidadeMedidaId"
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
                    </label>
                  ) : (
                    <p className="form-help">
                      {insumoSelecionado ? `Unidade: ${insumoSelecionado.unidadeMedidaPrimariaSigla}` : ''}
                    </p>
                  )}

                  <button
                    type="button"
                    className="btn-row btn-row--danger consumo-estoque__remove-item"
                    onClick={() => removeItem(index)}
                    disabled={form.itens.length === 1}
                  >
                    Remover
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
            <input type="datetime-local" name="dataConsumo" value={form.dataConsumo} onChange={handleFormChange} />
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
        <h3>Log de movimentações</h3>
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
                      {podeCancelar(c) ? (
                        <button type="button" className="btn-row btn-row--danger" onClick={() => abrirCancelamento(c)}>
                          Cancelar
                        </button>
                      ) : (
                        <span>—</span>
                      )}
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
    </div>
  )
}

export default ConsumoEstoqueTab
