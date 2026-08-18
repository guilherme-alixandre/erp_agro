import { useCallback, useEffect, useMemo, useState } from 'react'
import { listarSetores } from '../../setores/integration/setorApi'
import { listarConsumoPorSetor, registrarConsumo } from '../integration/consumoInsumoApi'

const defaultForm = {
  setorId: '',
  insumoId: '',
  quantidade: '',
  unidadeMedidaId: '',
  dataConsumo: '',
}

function formatarData(iso) {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return iso
  return data.toLocaleString('pt-BR')
}

function AlimentarLoteTab({ currentUser, insumosEstoque }) {
  const [setores, setSetores] = useState([])
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [resultado, setResultado] = useState(null)
  const [historico, setHistorico] = useState([])
  const [isLoadingHistorico, setIsLoadingHistorico] = useState(false)

  useEffect(() => {
    listarSetores()
      .then(setSetores)
      .catch(() => setSetores([]))
  }, [])

  const insumoSelecionado = useMemo(
    () => insumosEstoque.find((i) => String(i.id) === String(form.insumoId)) ?? null,
    [insumosEstoque, form.insumoId],
  )

  const fetchHistorico = useCallback(async (setorId) => {
    if (!setorId) {
      setHistorico([])
      return
    }
    setIsLoadingHistorico(true)
    try {
      const lista = await listarConsumoPorSetor(setorId)
      setHistorico(lista)
    } catch {
      setHistorico([])
    } finally {
      setIsLoadingHistorico(false)
    }
  }, [])

  useEffect(() => {
    fetchHistorico(form.setorId)
  }, [form.setorId, fetchHistorico])

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
    <div className="alimentar-lote">
      <div className="alimentar-lote__form-card">
        <h2>Alimentar Lote</h2>
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
          <dl className="details-grid alimentar-lote__resultado">
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

      <div className="alimentar-lote__historico">
        <h3>Histórico do setor</h3>
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
              </tr>
            </thead>
            <tbody>
              {!form.setorId ? (
                <tr>
                  <td colSpan={6} className="table-empty">Selecione um setor para ver o histórico.</td>
                </tr>
              ) : isLoadingHistorico ? (
                <tr>
                  <td colSpan={6} className="table-loading">Carregando...</td>
                </tr>
              ) : historico.length === 0 ? (
                <tr>
                  <td colSpan={6} className="table-empty">Nenhum consumo registrado neste setor.</td>
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
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

export default AlimentarLoteTab
