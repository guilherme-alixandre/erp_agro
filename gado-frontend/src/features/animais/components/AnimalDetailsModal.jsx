import { useEffect, useState } from 'react'
import { listarVacinacoesPorAnimal } from '../../insumos/integration/vacinacaoAnimalApi'

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

function AnimalDetailsModal({ animal, onClose, onEdit, onDelete, isDeleting }) {
  const [vacinacoes, setVacinacoes] = useState([])
  const [isLoadingVacinacoes, setIsLoadingVacinacoes] = useState(false)

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
