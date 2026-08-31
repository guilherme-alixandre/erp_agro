import { useState } from 'react'
import { loginUsuario } from '../../configuracoes/integration/usuarioApi'
import '../../animais/styles/animais.css'
import '../styles/auth.css'

const defaultLoginForm = {
  email: '',
  senha: '',
}

function AuthPage({ onLogin, sessionFeedback }) {
  const [loginForm, setLoginForm] = useState(defaultLoginForm)
  const [isLogando, setIsLogando] = useState(false)
  const [loginFeedback, setLoginFeedback] = useState({ type: '', message: '' })
  const [showLoginSenha, setShowLoginSenha] = useState(false)

  function handleLoginChange(event) {
    const { name, value } = event.target
    setLoginForm((current) => ({ ...current, [name]: value }))
  }

  async function handleLoginSubmit(event) {
    event.preventDefault()
    setIsLogando(true)
    setLoginFeedback({ type: '', message: '' })

    try {
      const { usuario, token } = await loginUsuario(loginForm.email, loginForm.senha)
      onLogin(usuario, token)
      setLoginForm(defaultLoginForm)
    } catch (error) {
      setLoginFeedback({
        type: 'error',
        message: error.message || 'Falha ao realizar login.',
      })
    } finally {
      setIsLogando(false)
    }
  }

  return (
    <main className="auth-layout">
      <article className="auth-card">
        <div className="auth-brand">
          <span className="auth-logo"><img src="/logo.png" alt="GADO" /></span>
          <h1>GADO · Gestão rural</h1>
        </div>

        <h2>Entrar</h2>
        <p className="auth-subtitle">Sua fazenda organizada em um só lugar.</p>

        <form className="animal-form" onSubmit={handleLoginSubmit}>
          <label>
            <span>
              E-mail <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="email"
              name="email"
              value={loginForm.email}
              onChange={handleLoginChange}
              required
            />
          </label>

          <label>
            <span>
              Senha <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <div className="password-field">
              <input
                type={showLoginSenha ? 'text' : 'password'}
                name="senha"
                value={loginForm.senha}
                onChange={handleLoginChange}
                required
              />
              <button
                type="button"
                className="password-field__toggle"
                onClick={() => setShowLoginSenha((v) => !v)}
                aria-label={showLoginSenha ? 'Ocultar senha' : 'Mostrar senha'}
              >
                {showLoginSenha ? 'Ocultar' : 'Exibir'}
              </button>
            </div>
          </label>

          {loginFeedback.message ? (
            <p
              className={`feedback ${loginFeedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
            >
              {loginFeedback.message}
            </p>
          ) : null}

          {!loginFeedback.message && sessionFeedback ? (
            <p className="feedback feedback--info">{sessionFeedback}</p>
          ) : null}

          <div className="modal-actions">
            <button type="submit" className="btn-primary" disabled={isLogando}>
              {isLogando ? 'Entrando...' : 'Entrar'}
            </button>
          </div>
        </form>

        <p className="auth-helper">
          Acesso restrito. Solicite ao administrador para criar sua conta.
        </p>
      </article>
    </main>
  )
}

export default AuthPage
