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
  const [form, setForm] = useState({
    nome: usuario.nome,
    perfil: usuario.perfil,
    novaSenha: '',
    confirmarSenha: '',
  })
  const [showNova, setShowNova] = useState(false)
  const [showConfirmar, setShowConfirmar] = useState(false)
  const [senhaError, setSenhaError] = useState('')

  function handleChange(e) {
    const { name, value } = e.target
    setForm((f) => ({ ...f, [name]: value }))
    if (name === 'novaSenha' || name === 'confirmarSenha') setSenhaError('')
  }

  function handleSubmit(e) {
    e.preventDefault()
    if (form.novaSenha || form.confirmarSenha) {
      if (form.novaSenha.length < 4) {
        setSenhaError('A nova senha deve ter ao menos 4 caracteres.')
        return
      }
      if (form.novaSenha !== form.confirmarSenha) {
        setSenhaError('As senhas não coincidem.')
        return
      }
    }
    setSenhaError('')
    const payload = { nome: form.nome, perfil: form.perfil }
    if (form.novaSenha) payload.senha = form.novaSenha
    onSubmit(payload)
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
            <input
              type="email"
              value={usuario.email}
              disabled
              style={{ background: '#f3f4f6', color: '#6b7280', cursor: 'not-allowed' }}
            />
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

          <fieldset className="senha-fieldset">
            <legend>Alterar senha</legend>
            <p className="form-help">Deixe em branco para não alterar.</p>

            <label>
              <span>Nova senha</span>
              <div className="password-field">
                <input
                  type={showNova ? 'text' : 'password'}
                  name="novaSenha"
                  value={form.novaSenha}
                  onChange={handleChange}
                  minLength={4}
                  placeholder="Mínimo 4 caracteres"
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  className="password-field__toggle"
                  onClick={() => setShowNova((v) => !v)}
                  aria-label={showNova ? 'Ocultar senha' : 'Mostrar senha'}
                >
                  {showNova ? 'Ocultar' : 'Exibir'}
                </button>
              </div>
            </label>

            <label>
              <span>Confirmar nova senha</span>
              <div className="password-field">
                <input
                  type={showConfirmar ? 'text' : 'password'}
                  name="confirmarSenha"
                  value={form.confirmarSenha}
                  onChange={handleChange}
                  placeholder="Repita a nova senha"
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  className="password-field__toggle"
                  onClick={() => setShowConfirmar((v) => !v)}
                  aria-label={showConfirmar ? 'Ocultar confirmação' : 'Mostrar confirmação'}
                >
                  {showConfirmar ? 'Ocultar' : 'Exibir'}
                </button>
              </div>
            </label>

            {senhaError ? (
              <p className="feedback feedback--error">{senhaError}</p>
            ) : null}
          </fieldset>

          {feedback ? (
            <p className="feedback feedback--error">{feedback}</p>
          ) : null}

          <div className="modal-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={onClose}
              disabled={isSaving}
            >
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
