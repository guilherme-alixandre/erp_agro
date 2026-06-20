import { useState } from 'react'

function TransferenciaAnimalModal({
  animal,
  loteAtual,
  setorAtual,
  lotesDisponiveis,
  setoresDisponiveis,
  isSaving,
  feedback,
  onConfirm,
  onClose,
}) {
  const [loteDestinoId, setLoteDestinoId] = useState('')
  const [setorDestinoId, setSetorDestinoId] = useState('')

  const mesmoLoteSetor =
    loteDestinoId !== '' &&
    setorDestinoId !== '' &&
    Number(loteDestinoId) === loteAtual.id &&
    Number(setorDestinoId) === setorAtual.id

  function handleSubmit(e) {
    e.preventDefault()
    if (!loteDestinoId || !setorDestinoId || mesmoLoteSetor) return
    onConfirm(Number(loteDestinoId), Number(setorDestinoId))
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Transferir animal</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <dl className="details-grid">
          <div>
            <dt>Animal</dt>
            <dd>
              <strong>{animal.codigoBrinco}</strong>
              {animal.nome ? ` — ${animal.nome}` : ''}
            </dd>
          </div>
          <div>
            <dt>Lote atual</dt>
            <dd>{loteAtual.codigo}</dd>
          </div>
          <div>
            <dt>Setor atual</dt>
            <dd>{setorAtual.nome}</dd>
          </div>
        </dl>

        <form onSubmit={handleSubmit}>
          <label>
            <span>
              Lote de destino{' '}
              <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select
              value={loteDestinoId}
              onChange={(e) => setLoteDestinoId(e.target.value)}
              required
            >
              <option value="">Selecione um lote...</option>
              {lotesDisponiveis.map((l) => (
                <option key={l.id} value={l.id}>
                  {l.codigo}{l.descricao ? ` — ${l.descricao}` : ''}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Setor de destino{' '}
              <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select
              value={setorDestinoId}
              onChange={(e) => setSetorDestinoId(e.target.value)}
              required
            >
              <option value="">Selecione um setor...</option>
              {setoresDisponiveis.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nome}
                </option>
              ))}
            </select>
          </label>

          {mesmoLoteSetor ? (
            <p className="feedback feedback--error">
              O animal já está neste lote e setor. Escolha um destino diferente.
            </p>
          ) : null}

          {feedback && !mesmoLoteSetor ? (
            <p className="feedback feedback--error">{feedback}</p>
          ) : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={isSaving || !loteDestinoId || !setorDestinoId || mesmoLoteSetor}
            >
              {isSaving ? 'Transferindo...' : 'Confirmar transferência'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default TransferenciaAnimalModal
