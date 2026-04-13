function AnimalCard({ animal, onDetalhes, onEditar }) {
  return (
    <article className="animal-card">
      <div className="animal-card__top">
        <span>Etiqueta: {animal.codigoBrinco}</span>
        <span>Peso</span>
      </div>

      <div className="animal-card__middle">
        <strong>
          {animal.nome || 'Sem nome'} ({animal.idadeLabel})
        </strong>
        <strong>{animal.pesoLabel}</strong>
      </div>

      <p className="animal-card__vaccines">
        Vacinas: {animal.vacinas || 'Sem vacinas cadastradas'}
      </p>

      <div className="animal-card__actions">
        <button type="button" onClick={() => onDetalhes(animal)}>
          Detalhes
        </button>
        <button type="button" onClick={() => onEditar(animal)}>
          Editar
        </button>
      </div>
    </article>
  )
}

export default AnimalCard
