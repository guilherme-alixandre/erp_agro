import { useState } from 'react'

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const SENHA_MIN_LENGTH = 6

const PERFIL_OPTIONS = ['GERENTE', 'CUIDADOR', 'CUIDADOR_CHEFE', 'FINANCEIRO', 'ADMINISTRADOR']

const PERFIL_LABELS = {
  ADMINISTRADOR: 'Administrador',
  GERENTE: 'Gerente',
  CUIDADOR: 'Cuidador',
  CUIDADOR_CHEFE: 'Cuidador Chefe',
  FINANCEIRO: 'Financeiro',
}

function UsuarioFormModal({
  formData,
  isSaving,
  feedback,
  onClose,
  onChange,
  onSubmit,
}) {
  const [showSenha, setShowSenha] = useState(false)
  const [showConfirmar, setShowConfirmar] = useState(false)
  const [confirmarSenha, setConfirmarSenha] = useState('')
  const [senhaError, setSenhaError] = useState('')
  const [emailError, setEmailError] = useState('')
  const [senhaMinError, setSenhaMinError] = useState('')

  function handleEmailChange(event) {
    onChange(event)
    const valor = event.target.value
    setEmailError(valor && !EMAIL_REGEX.test(valor) ? 'Digite um e-mail válido.' : '')
  }

  function handleSenhaChange(event) {
    onChange(event)
    setSenhaError('')
    const valor = event.target.value
    setSenhaMinError(
      valor && valor.length < SENHA_MIN_LENGTH ? 'A senha deve possuir 6 ou mais caracteres.' : '',
    )
  }

  function handleLocalSubmit(event) {
    event.preventDefault()
    if (formData.email && !EMAIL_REGEX.test(formData.email)) {
      setEmailError('Digite um e-mail válido.')
      return
    }
    if (formData.senha && formData.senha.length < SENHA_MIN_LENGTH) {
      setSenhaMinError('A senha deve possuir 6 ou mais caracteres.')
      return
    }
    if (formData.senha !== confirmarSenha) {
      setSenhaError('As senhas não coincidem.')
      return
    }
    setSenhaError('')
    onSubmit(event)
  }

  const hasBlockingError = Boolean(emailError) || Boolean(senhaMinError)

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Cadastrar usuário</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={handleLocalSubmit}>
          <p className="form-info">
            O usuário poderá entrar usando o e-mail e a senha definidos aqui.
            Compartilhe os dados com ele de forma segura.
          </p>

          <label>
            <span>
              Nome <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>
              E-mail <span className="required-marker" aria-hidden="true">*</span>
            </span>
            {emailError ? <span className="field-error-msg">{emailError}</span> : null}
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleEmailChange}
              required
            />
          </label>

          <label>
            <span>
              Senha inicial <span className="required-marker" aria-hidden="true">*</span>
            </span>
            {senhaMinError ? <span className="field-error-msg">{senhaMinError}</span> : null}
            <div className="password-field">
              <input
                type={showSenha ? 'text' : 'password'}
                name="senha"
                value={formData.senha}
                onChange={handleSenhaChange}
                minLength={SENHA_MIN_LENGTH}
                required
              />
              <button
                type="button"
                className="password-field__toggle"
                onClick={() => setShowSenha((v) => !v)}
                aria-label={showSenha ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showSenha ? 'Ocultar' : 'Exibir'}
              </button>
            </div>
          </label>

          <label>
            <span>
              Confirmar senha <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <div className="password-field">
              <input
                type={showConfirmar ? 'text' : 'password'}
                name="confirmarSenha"
                value={confirmarSenha}
                onChange={(e) => { setConfirmarSenha(e.target.value); setSenhaError('') }}
                minLength={SENHA_MIN_LENGTH}
                required
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
            {senhaError ? <span className="field-error-msg">{senhaError}</span> : null}
          </label>

          <label>
            <span>
              Perfil <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="perfil" value={formData.perfil} onChange={onChange}>
              {PERFIL_OPTIONS.map((perfil) => (
                <option key={perfil} value={perfil}>
                  {PERFIL_LABELS[perfil] ?? perfil}
                </option>
              ))}
            </select>
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving || hasBlockingError}>
              {isSaving ? 'Cadastrando...' : 'Cadastrar usuário'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default UsuarioFormModal
