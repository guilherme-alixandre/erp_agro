import { useState } from 'react'

function ImportarNfeModal({ isSaving, feedback, onClose, onSubmit }) {
  const [file, setFile] = useState(null)

  function handleSubmit(event) {
    event.preventDefault()
    if (!file) return
    onSubmit(file)
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Importar NF-e (XML)</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <p className="form-info">
          Uma NF-e é um documento fiscal já validado — ao importar, ela é cadastrada como
          <strong> APROVADA</strong> automaticamente e já entra no resumo financeiro do mês.
        </p>

        <form className="animal-form" onSubmit={handleSubmit}>
          <label>
            <span>
              Arquivo XML <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="file"
              accept=".xml,text/xml,application/xml"
              onChange={(e) => setFile(e.target.files?.[0] ?? null)}
              required
            />
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving || !file}>
              {isSaving ? 'Importando...' : 'Importar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default ImportarNfeModal
