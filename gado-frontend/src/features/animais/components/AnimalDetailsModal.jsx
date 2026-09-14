import { useEffect, useState } from 'react'
import { listarVacinacoesPorAnimal } from '../../insumos/integration/vacinacaoAnimalApi'
import {
  listarOcorrenciasPorAnimal,
  cadastrarOcorrencia,
  atualizarOcorrencia,
  excluirOcorrencia,
  TIPOS_OCORRENCIA,
} from '../integration/ocorrenciaAnimalApi'
import { formatarData, formatarDataHora } from '../../../utils/formatters'

const STATUS_LABELS = {
  ATIVO: 'Ativo',
  OBSERVACAO: 'Em observação',
  VENDIDO: 'Vendido',
  OBITO: 'Óbito',
  ABATIDO: 'Abatido',
}

function formatCm(value) {
  if (value === null || value === undefined || value === '') return '—'
  const num = Number(value)
  if (!Number.isFinite(num)) return '—'
  return `${num.toLocaleString('pt-BR', { maximumFractionDigits: 2 })} cm`
}

const TIPO_OCORRENCIA_LABELS = TIPOS_OCORRENCIA.reduce((acc, t) => ({ ...acc, [t.value]: t.label }), {})

const defaultOcorrenciaForm = { tipoOcorrencia: 'DOENCA', dataOcorrencia: '', observacao: '' }

function toDateInputValue(valor) {
  if (!valor) return ''
  const partes = /^(\d{4}-\d{2}-\d{2})/.exec(String(valor))
  return partes ? partes[1] : ''
}

function AnimalDetailsModal({ animal, onClose, onEdit, onDelete, isDeleting }) {
  const [vacinacoes, setVacinacoes] = useState([])
  const [isLoadingVacinacoes, setIsLoadingVacinacoes] = useState(false)

  const [ocorrencias, setOcorrencias] = useState([])
  const [isLoadingOcorrencias, setIsLoadingOcorrencias] = useState(false)
  const [isAddingOcorrencia, setIsAddingOcorrencia] = useState(false)
  const [ocorrenciaForm, setOcorrenciaForm] = useState(defaultOcorrenciaForm)
  const [isSavingOcorrencia, setIsSavingOcorrencia] = useState(false)
  const [ocorrenciaErro, setOcorrenciaErro] = useState('')
  const [editingOcorrenciaId, setEditingOcorrenciaId] = useState(null)
  const [deletingOcorrenciaId, setDeletingOcorrenciaId] = useState(null)

  useEffect(() => {
    if (!animal.id) {
      setVacinacoes([])
      return
    }
    let cancelado = false
    setIsLoadingVacinacoes(true)
    listarVacinacoesPorAnimal(animal.id)
      .then((lista) => {
        if (!cancelado) setVacinacoes(lista)
      })
      .catch(() => {
        if (!cancelado) setVacinacoes([])
      })
      .finally(() => {
        if (!cancelado) setIsLoadingVacinacoes(false)
      })
    return () => {
      cancelado = true
    }
  }, [animal.id])

  function carregarOcorrencias() {
    if (!animal.id) {
      setOcorrencias([])
      return
    }
    setIsLoadingOcorrencias(true)
    listarOcorrenciasPorAnimal(animal.id)
      .then((lista) => setOcorrencias(lista))
      .catch(() => setOcorrencias([]))
      .finally(() => setIsLoadingOcorrencias(false))
  }

  useEffect(carregarOcorrencias, [animal.id])

  function handleOcorrenciaChange(event) {
    const { name, value } = event.target
    setOcorrenciaForm((current) => ({ ...current, [name]: value }))
  }

  async function handleSalvarOcorrencia(event) {
    event.preventDefault()
    if (!ocorrenciaForm.dataOcorrencia) {
      setOcorrenciaErro('Informe a data da ocorrência.')
      return
    }
    setIsSavingOcorrencia(true)
    setOcorrenciaErro('')
    try {
      if (editingOcorrenciaId) {
        await atualizarOcorrencia(editingOcorrenciaId, ocorrenciaForm)
      } else {
        await cadastrarOcorrencia(animal.id, ocorrenciaForm)
      }
      setOcorrenciaForm(defaultOcorrenciaForm)
      setEditingOcorrenciaId(null)
      setIsAddingOcorrencia(false)
      carregarOcorrencias()
    } catch (error) {
      setOcorrenciaErro(error.message || 'Falha ao salvar a ocorrência.')
    } finally {
      setIsSavingOcorrencia(false)
    }
  }

  function handleEditarOcorrencia(ocorrencia) {
    setEditingOcorrenciaId(ocorrencia.id)
    setOcorrenciaForm({
      tipoOcorrencia: ocorrencia.tipoOcorrencia,
      dataOcorrencia: toDateInputValue(ocorrencia.dataOcorrencia),
      observacao: ocorrencia.observacao ?? '',
    })
    setOcorrenciaErro('')
    setIsAddingOcorrencia(true)
  }

  function handleCancelarFormOcorrencia() {
    setIsAddingOcorrencia(false)
    setEditingOcorrenciaId(null)
    setOcorrenciaErro('')
    setOcorrenciaForm(defaultOcorrenciaForm)
  }

  async function handleExcluirOcorrencia(ocorrencia) {
    const confirmar = window.confirm('Deseja excluir esta ocorrência?')
    if (!confirmar) return

    setDeletingOcorrenciaId(ocorrencia.id)
    try {
      await excluirOcorrencia(ocorrencia.id)
      if (editingOcorrenciaId === ocorrencia.id) {
        handleCancelarFormOcorrencia()
      }
      carregarOcorrencias()
    } catch (error) {
      setOcorrenciaErro(error.message || 'Falha ao excluir a ocorrência.')
    } finally {
      setDeletingOcorrenciaId(null)
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Detalhes do animal</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <dl className="details-grid">
          <div>
            <dt>Brinco</dt>
            <dd>{animal.codigoBrinco}</dd>
          </div>
          <div>
            <dt>Data de nascimento</dt>
            <dd>{formatarData(animal.dataNascimento)}</dd>
          </div>
          <div>
            <dt>Peso</dt>
            <dd>{animal.pesoLabel}</dd>
          </div>
          <div>
            <dt>Raça</dt>
            <dd>{animal.racaNome || '-'}</dd>
          </div>
          <div>
            <dt>Cor</dt>
            <dd>{animal.cor || '-'}</dd>
          </div>
          <div>
            <dt>Altura na cernelha</dt>
            <dd>{formatCm(animal.alturaCernelha)}</dd>
          </div>
          <div>
            <dt>Perímetro torácico</dt>
            <dd>{formatCm(animal.perimetroToracico)}</dd>
          </div>
          <div>
            <dt>Comprimento corporal</dt>
            <dd>{formatCm(animal.comprimentoCorporal)}</dd>
          </div>
          <div>
            <dt>Sexo</dt>
            <dd>{animal.sexo === 'F' ? 'Fêmea' : 'Macho'}</dd>
          </div>
          <div>
            <dt>Status</dt>
            <dd>{STATUS_LABELS[animal.statusAnimal] ?? animal.statusAnimal}</dd>
          </div>
        </dl>

        <h3 className="details-section">Vacinas aplicadas</h3>
        {isLoadingVacinacoes ? (
          <p className="vacinas-empty">Carregando...</p>
        ) : vacinacoes.length > 0 ? (
          <ul className="vacinas-detail-list">
            {vacinacoes.map((v) => (
              <li key={v.id}>
                <strong>
                  {v.insumoNome}
                  {v.cancelado ? ' (cancelada)' : ''}
                </strong>
                <span>
                  {formatarDataHora(v.dataAplicacao)} — {v.quantidadePorAnimal} {v.unidadeRegistroSigla}
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="vacinas-empty">Nenhuma vacina aplicada a este animal.</p>
        )}

        <h3 className="details-section">Ocorrências</h3>
        {isLoadingOcorrencias ? (
          <p className="vacinas-empty">Carregando...</p>
        ) : ocorrencias.length > 0 ? (
          <ul className="vacinas-detail-list">
            {ocorrencias.map((o) => (
              <li key={o.id} className="ocorrencia-item">
                <strong>{TIPO_OCORRENCIA_LABELS[o.tipoOcorrencia] ?? o.tipoOcorrencia}</strong>
                <span>
                  {formatarData(o.dataOcorrencia)}
                  {o.observacao ? ` — ${o.observacao}` : ''}
                </span>
                <div className="row-actions">
                  <button
                    type="button"
                    className="btn-row btn-row--edit"
                    onClick={() => handleEditarOcorrencia(o)}
                  >
                    Editar
                  </button>
                  <button
                    type="button"
                    className="btn-row btn-row--danger"
                    onClick={() => handleExcluirOcorrencia(o)}
                    disabled={deletingOcorrenciaId === o.id}
                  >
                    {deletingOcorrenciaId === o.id ? 'Excluindo...' : 'Excluir'}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p className="vacinas-empty">Nenhuma ocorrência registrada para este animal.</p>
        )}

        {isAddingOcorrencia ? (
          <form className="animal-form" onSubmit={handleSalvarOcorrencia}>
            <label>
              <span>Tipo</span>
              <select name="tipoOcorrencia" value={ocorrenciaForm.tipoOcorrencia} onChange={handleOcorrenciaChange}>
                {TIPOS_OCORRENCIA.map((t) => (
                  <option key={t.value} value={t.value}>
                    {t.label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              <span>Data</span>
              <input
                type="date"
                name="dataOcorrencia"
                value={ocorrenciaForm.dataOcorrencia}
                onChange={handleOcorrenciaChange}
                required
              />
            </label>
            <label>
              <span>Observação</span>
              <textarea name="observacao" value={ocorrenciaForm.observacao} onChange={handleOcorrenciaChange} rows={2} />
            </label>
            {ocorrenciaErro ? <p className="feedback feedback--error">{ocorrenciaErro}</p> : null}
            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={handleCancelarFormOcorrencia}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary" disabled={isSavingOcorrencia}>
                {isSavingOcorrencia
                  ? 'Salvando...'
                  : editingOcorrenciaId
                    ? 'Salvar alterações'
                    : 'Salvar ocorrência'}
              </button>
            </div>
          </form>
        ) : (
          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={() => setIsAddingOcorrencia(true)}>
              + Registrar ocorrência
            </button>
          </div>
        )}

        <div className="modal-actions">
          <button type="button" className="btn-secondary" onClick={onEdit}>
            Editar
          </button>
          <button
            type="button"
            className="btn-danger"
            onClick={() => onDelete(animal)}
            disabled={isDeleting}
          >
            {isDeleting ? 'Excluindo...' : 'Excluir'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default AnimalDetailsModal
