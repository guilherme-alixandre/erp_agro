import { useEffect, useState } from 'react'
import { listarVacinacoesPorAnimal } from '../../insumos/integration/vacinacaoAnimalApi'
import { listarOcorrenciasPorAnimal, cadastrarOcorrencia, TIPOS_OCORRENCIA } from '../integration/ocorrenciaAnimalApi'

function formatDate(dateText) {
  if (!dateText) return '-'
  const [year, month, day] = dateText.split('-')
  return `${day}/${month}/${year}`
}

function formatDateTime(iso) {
  if (!iso) return '—'
  const data = new Date(iso)
  if (Number.isNaN(data.getTime())) return iso
  return data.toLocaleString('pt-BR')
}

function formatCm(value) {
  if (value === null || value === undefined || value === '') return '-'
  const num = Number(value)
  if (!Number.isFinite(num)) return '-'
  return `${num} cm`
}

const TIPO_OCORRENCIA_LABELS = TIPOS_OCORRENCIA.reduce((acc, t) => ({ ...acc, [t.value]: t.label }), {})

const defaultOcorrenciaForm = { tipoOcorrencia: 'DOENCA', dataOcorrencia: '', observacao: '' }

function AnimalDetailsModal({ animal, onClose, onEdit, onDelete, isDeleting }) {
  const [vacinacoes, setVacinacoes] = useState([])
  const [isLoadingVacinacoes, setIsLoadingVacinacoes] = useState(false)

  const [ocorrencias, setOcorrencias] = useState([])
  const [isLoadingOcorrencias, setIsLoadingOcorrencias] = useState(false)
  const [isAddingOcorrencia, setIsAddingOcorrencia] = useState(false)
  const [ocorrenciaForm, setOcorrenciaForm] = useState(defaultOcorrenciaForm)
  const [isSavingOcorrencia, setIsSavingOcorrencia] = useState(false)
  const [ocorrenciaErro, setOcorrenciaErro] = useState('')

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
      await cadastrarOcorrencia(animal.id, ocorrenciaForm)
      setOcorrenciaForm(defaultOcorrenciaForm)
      setIsAddingOcorrencia(false)
      carregarOcorrencias()
    } catch (error) {
      setOcorrenciaErro(error.message || 'Falha ao registrar a ocorrência.')
    } finally {
      setIsSavingOcorrencia(false)
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Detalhes do animal</h2>
          <button type="button" className="modal-close" onClick={onClose}>
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
            <dd>{formatDate(animal.dataNascimento)}</dd>
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
            <dd>{animal.statusAnimal}</dd>
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
                  {formatDateTime(v.dataAplicacao)} — {v.quantidadePorAnimal} {v.unidadeRegistroSigla}
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
              <li key={o.id}>
                <strong>{TIPO_OCORRENCIA_LABELS[o.tipoOcorrencia] ?? o.tipoOcorrencia}</strong>
                <span>
                  {formatDateTime(o.dataOcorrencia)}
                  {o.observacao ? ` — ${o.observacao}` : ''}
                </span>
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
              <button
                type="button"
                className="btn-secondary"
                onClick={() => {
                  setIsAddingOcorrencia(false)
                  setOcorrenciaErro('')
                  setOcorrenciaForm(defaultOcorrenciaForm)
                }}
              >
                Cancelar
              </button>
              <button type="submit" className="btn-primary" disabled={isSavingOcorrencia}>
                {isSavingOcorrencia ? 'Salvando...' : 'Salvar ocorrência'}
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
