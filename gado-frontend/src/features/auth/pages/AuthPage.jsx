import { useState } from 'react'
import { cadastrarUsuario, loginUsuario } from '../../../services/usuarioApi'
import '../../animais/styles/animais.css'
import '../styles/auth.css'

const PERFIL_OPTIONS = ['GERENTE', 'CASEIRO', 'ADMINISTRADOR']

const defaultCadastroForm = {
  nome: '',
  email: '',
  senha: '',
  perfil: 'GERENTE',
}

const defaultLoginForm = {
  email: '',
  senha: '',
}

function AuthPage({ onLogin, sessionFeedback }) {
  const [cadastroForm, setCadastroForm] = useState(defaultCadastroForm)
  const [loginForm, setLoginForm] = useState(defaultLoginForm)
  const [showCadastroForm, setShowCadastroForm] = useState(false)
  const [isCadastrando, setIsCadastrando] = useState(false)
  const [isLogando, setIsLogando] = useState(false)
  const [cadastroFeedback, setCadastroFeedback] = useState({ type: '', message: '' })
  const [loginFeedback, setLoginFeedback] = useState({ type: '', message: '' })

  function handleCadastroChange(event) {
    const { name, value } = event.target
    setCadastroForm((current) => ({ ...current, [name]: value }))
  }

  function handleLoginChange(event) {
    const { name, value } = event.target
    setLoginForm((current) => ({ ...current, [name]: value }))
  }

  async function handleCadastroSubmit(event) {
    event.preventDefault()
    setIsCadastrando(true)
    setCadastroFeedback({ type: '', message: '' })

    try {
      await cadastrarUsuario(cadastroForm)
      setCadastroForm(defaultCadastroForm)
      setShowCadastroForm(false)
      setLoginFeedback({
        type: 'info',
        message: 'Cadastro realizado com sucesso. Faça login para continuar.',
      })
    } catch (error) {
      setCadastroFeedback({
        type: 'error',
        message: error.message || 'Falha ao cadastrar usuário.',
      })
    } finally {
      setIsCadastrando(false)
    }
  }

  async function handleLoginSubmit(event) {
    event.preventDefault()
    setIsLogando(true)
    setLoginFeedback({ type: '', message: '' })

    try {
      const usuario = await loginUsuario(loginForm.email, loginForm.senha)
      onLogin(usuario)
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
          <span className="auth-logo">🌿</span>
          <h1>ERP Agro</h1>
        </div>

        <h2>{showCadastroForm ? 'Cadastre-se' : 'Entrar'}</h2>
        <p className="auth-subtitle">
          {showCadastroForm
            ? 'Crie sua conta para acessar o sistema.'
            : 'Acesse sua conta para continuar.'}
        </p>

        {showCadastroForm ? (
          <form className="animal-form" onSubmit={handleCadastroSubmit}>
            <label>
              <span>Nome</span>
              <input
                type="text"
                name="nome"
                value={cadastroForm.nome}
                onChange={handleCadastroChange}
                required
              />
            </label>

            <label>
              <span>E-mail</span>
              <input
                type="email"
                name="email"
                value={cadastroForm.email}
                onChange={handleCadastroChange}
                required
              />
            </label>

            <label>
              <span>Senha</span>
              <input
                type="password"
                name="senha"
                value={cadastroForm.senha}
                onChange={handleCadastroChange}
                required
              />
            </label>

            <label>
              <span>Perfil</span>
              <select
                name="perfil"
                value={cadastroForm.perfil}
                onChange={handleCadastroChange}
              >
                {PERFIL_OPTIONS.map((perfil) => (
                  <option key={perfil} value={perfil}>
                    {perfil}
                  </option>
                ))}
              </select>
            </label>

            {cadastroFeedback.message ? (
              <p
                className={`feedback ${cadastroFeedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
              >
                {cadastroFeedback.message}
              </p>
            ) : null}

            <div className="modal-actions">
              <button type="submit" className="btn-primary" disabled={isCadastrando}>
                {isCadastrando ? 'Cadastrando...' : 'Cadastrar'}
              </button>
            </div>
          </form>
        ) : (
          <form className="animal-form" onSubmit={handleLoginSubmit}>
            <label>
              <span>E-mail</span>
              <input
                type="email"
                name="email"
                value={loginForm.email}
                onChange={handleLoginChange}
                required
              />
            </label>

            <label>
              <span>Senha</span>
              <input
                type="password"
                name="senha"
                value={loginForm.senha}
                onChange={handleLoginChange}
                required
              />
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
        )}

        <div className="auth-toggle">
          <p>{showCadastroForm ? 'Já tem cadastro?' : 'Não tem cadastro?'}</p>
          <button
            type="button"
            className="btn-secondary"
            onClick={() => setShowCadastroForm((current) => !current)}
          >
            {showCadastroForm ? 'Voltar para login' : 'Cadastre-se'}
          </button>
        </div>
      </article>
    </main>
  )
}

export default AuthPage
