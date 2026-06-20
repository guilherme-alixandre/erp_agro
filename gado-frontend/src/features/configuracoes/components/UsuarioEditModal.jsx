import { useState } from 'react'

const PERFIL_OPTIONS = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR', 'CUIDADOR_CHEFE', 'FINANCEIRO']

const PERFIL_LABELS = {
  ADMINISTRADOR: 'Administrador',
  GERENTE: 'Gerente',
  CUIDADOR: 'Cuidador',
  CUIDADOR_CHEFE: 'Cuidador Chefe',
  FINANCEIRO: 'Financeiro',
}

function UsuarioEditModal({ usuario, isSaving, feedback, onClose, onSubmit }) {
  const [form, setForm] = useState({ nome: usuario.nome, perfil: usuario.perfil })

  function handleChange(e) {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
  }

  function handleSubmit(e) {
    e.preventDefault()
    onSubmit(form)
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Editar usuário</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={handleSubmit}>
          <label>
            <span>
              Nome <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="text"
              name="nome"
              value={form.nome}
              onChange={handleChange}
              required
              autoFocus
            />
          </label>

          <label>
            <span>E-mail</span>
            <input type="email" value={usuario.email} disabled style={{ background: '#f3f4f6', color: '#6b7280', cursor: 'not-allowed' }} />
          </label>

          <label>
            <span>
              Perfil <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="perfil" value={form.perfil} onChange={handleChange}>
              {PERFIL_OPTIONS.map((p) => (
                <option key={p} value={p}>
                  {PERFIL_LABELS[p] ?? p}
                </option>
              ))}
            </select>
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Salvando...' : 'Salvar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default UsuarioEditModal
