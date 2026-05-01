import { useState } from 'react'
import { cadastrarUsuario, loginUsuario } from '../../../services/usuarioApi'
import '../../animais/styles/animais.css'
import '../styles/auth.css'

const defaultLoginForm = {
  email: '',
  senha: '',
}

const defaultCadastroForm = {
  nome: '',
  email: '',
  senha: '',
  perfil: 'CUIDADOR',
}

function AuthPage({ onLogin, sessionFeedback }) {
  const [mode, setMode] = useState('login')

  const [loginForm, setLoginForm] = useState(defaultLoginForm)
  const [isLogando, setIsLogando] = useState(false)
  const [loginFeedback, setLoginFeedback] = useState({ type: '', message: '' })

  const [cadastroForm, setCadastroForm] = useState(defaultCadastroForm)
  const [isCadastrando, setIsCadastrando] = useState(false)
  const [cadastroFeedback, setCadastroFeedback] = useState({
    type: '',
    message: '',
  })

  function handleLoginChange(event) {
    const { name, value } = event.target
    setLoginForm((current) => ({ ...current, [name]: value }))
  }

  function handleCadastroChange(event) {
    const { name, value } = event.target
    setCadastroForm((current) => ({ ...current, [name]: value }))
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

  async function handleCadastroSubmit(event) {
    event.preventDefault()
    setIsCadastrando(true)
    setCadastroFeedback({ type: '', message: '' })

    try {
      const usuario = await cadastrarUsuario(cadastroForm)
      onLogin(usuario)
      setCadastroForm(defaultCadastroForm)
    } catch (error) {
      setCadastroFeedback({
        type: 'error',
        message: error.message || 'Falha ao realizar cadastro.',
      })
    } finally {
      setIsCadastrando(false)
    }
  }

  return (
    <main className="auth-layout">
      <article className="auth-card">
        <div className="auth-brand">
          <span className="auth-logo">🌿</span>
          <h1>ERP Agro</h1>
        </div>

        {mode === 'login' ? (
          <>
            <h2>Entrar</h2>
            <p className="auth-subtitle">Acesse sua conta para continuar.</p>

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

            <p className="auth-helper">
              Não tem conta?{' '}
              <button
                type="button"
                className="auth-link"
                onClick={() => {
                  setMode('cadastro')
                  setLoginFeedback({ type: '', message: '' })
                }}
              >
                Criar agora
              </button>
            </p>
          </>
        ) : (
          <>
            <h2>Criar conta</h2>
            <p className="auth-subtitle">Preencha seus dados para continuar.</p>

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
                  required
                >
                  <option value="CUIDADOR">CUIDADOR</option>
                  <option value="GERENTE">GERENTE</option>
                  <option value="FINANCEIRO">FINANCEIRO</option>
                  <option value="ADMINISTRADOR">ADMINISTRADOR</option>
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
                <button
                  type="submit"
                  className="btn-primary"
                  disabled={isCadastrando}
                >
                  {isCadastrando ? 'Criando...' : 'Criar conta'}
                </button>
              </div>
            </form>

            <p className="auth-helper">
              Já tem conta?{' '}
              <button
                type="button"
                className="auth-link"
                onClick={() => {
                  setMode('login')
                  setCadastroFeedback({ type: '', message: '' })
                }}
              >
                Voltar para login
              </button>
            </p>
          </>
        )}
      </article>
    </main>
  )
}

export default AuthPage

