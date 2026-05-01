function formatVacinasResumo(vacinas) {
  if (!Array.isArray(vacinas) || vacinas.length === 0) {
    return 'Sem vacinas cadastradas'
  }
  const nomes = vacinas
    .map((v) => v?.nome)
    .filter((n) => typeof n === 'string' && n.trim())
  if (nomes.length === 0) return `${vacinas.length} cadastrada(s)`
  if (nomes.length <= 2) return nomes.join(', ')
  return `${nomes.slice(0, 2).join(', ')} +${nomes.length - 2}`
}

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
        Vacinas: {formatVacinasResumo(animal.vacinas)}
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
