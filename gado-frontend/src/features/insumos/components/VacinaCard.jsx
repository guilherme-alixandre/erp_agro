function VacinaCard({ vacina, onEditar }) {
  return (
    <article className="animal-card vacina-card">
      <div className="vacina-card__header">
        <strong>{vacina.nome}</strong>
        {vacina.pendente ? (
          <span className="vacina-badge vacina-badge--pendente">Pendente</span>
        ) : null}
      </div>

      <div className="animal-card__actions">
        <button type="button" onClick={() => onEditar(vacina)}>
          Editar
        </button>
      </div>
    </article>
  )
}

export default VacinaCard
