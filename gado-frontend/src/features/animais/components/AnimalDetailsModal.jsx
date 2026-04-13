function formatDate(dateText) {
  if (!dateText) return '-'
  const [year, month, day] = dateText.split('-')
  return `${day}/${month}/${year}`
}

function AnimalDetailsModal({ animal, onClose, onEdit, onDelete, isDeleting }) {
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
            <dt>Nome</dt>
            <dd>{animal.nome || '-'}</dd>
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
            <dd>{animal.raca || '-'}</dd>
          </div>
          <div>
            <dt>Cor</dt>
            <dd>{animal.cor || '-'}</dd>
          </div>
          <div>
            <dt>Tamanho</dt>
            <dd>{animal.tamanho || '-'}</dd>
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
