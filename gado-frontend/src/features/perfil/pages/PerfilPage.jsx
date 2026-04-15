import { useState } from 'react'
import { cadastrarUsuario, loginUsuario } from '../../../services/usuarioApi'
import '../../animais/styles/animais.css'
import '../styles/perfil.css'

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

function PerfilPage({ currentUser, onLogin, onLogout, onNavigate }) {
  const [cadastroForm, setCadastroForm] = useState(defaultCadastroForm)
  const [loginForm, setLoginForm] = useState(defaultLoginForm)
  const [showCadastroForm, setShowCadastroForm] = useState(false)
  const [isCadastrando, setIsCadastrando] = useState(false)
  const [isLogando, setIsLogando] = useState(false)
  const [cadastroFeedback, setCadastroFeedback] = useState({ type: '', message: '' })
  const [loginFeedback, setLoginFeedback] = useState({ type: '', message: '' })
  const [sessionFeedback, setSessionFeedback] = useState({ type: '', message: '' })

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
      const responseMessage = await cadastrarUsuario(cadastroForm)
      setCadastroFeedback({
        type: 'info',
        message:
          typeof responseMessage === 'string'
            ? responseMessage
            : 'Usuário cadastrado com sucesso.',
      })
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
      setLoginFeedback({ type: 'info', message: 'Login realizado com sucesso.' })
      setSessionFeedback({ type: '', message: '' })
    } catch (error) {
      setLoginFeedback({
        type: 'error',
        message: error.message || 'Falha ao realizar login.',
      })
    } finally {
      setIsLogando(false)
    }
  }

  function handleLogoutClick() {
    onLogout()
    setSessionFeedback({ type: 'info', message: 'Você saiu da sessão com sucesso.' })
    setLoginFeedback({ type: '', message: '' })
  }

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo">🌿</div>
        <nav>
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button type="button" className="menu-item">
            Lotes
          </button>
          <button type="button" className="menu-item">
            Setores
          </button>
          <button type="button" className="menu-item">
            Insumos
          </button>
          <button type="button" className="menu-item">
            Financeiro
          </button>
          <button type="button" className="menu-item menu-item--active">
            Perfil
          </button>
        </nav>
        <div className="sidebar-user">
          <strong>{currentUser?.nome ?? 'Sem login'}</strong>
          <span>{currentUser?.email ?? 'Entre para acessar os recursos'}</span>
        </div>
      </aside>

      <section className="animals-content">
        <header className="animals-header">
          <h1>Perfil</h1>
          <span>{currentUser ? `Sessão ativa: ${currentUser.nome}` : 'Sessão inativa'}</span>
        </header>

        <div className="perfil-stack">
          <article className="animal-card perfil-card perfil-card--main">
            <h2>Entrar</h2>
            <p className="perfil-subtitle">Acesse sua conta para continuar.</p>

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

              {sessionFeedback.message ? (
                <p
                  className={`feedback ${sessionFeedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
                >
                  {sessionFeedback.message}
                </p>
              ) : null}

              <div className="modal-actions perfil-actions">
                {currentUser ? (
                  <button type="button" className="btn-secondary" onClick={handleLogoutClick}>
                    Sair
                  </button>
                ) : null}
                <button type="submit" className="btn-primary" disabled={isLogando}>
                  {isLogando ? 'Entrando...' : 'Entrar'}
                </button>
              </div>
            </form>

            <div className="perfil-toggle">
              <p>Não tem cadastro?</p>
              <button
                type="button"
                className="btn-secondary"
                onClick={() => setShowCadastroForm((current) => !current)}
              >
                {showCadastroForm ? 'Fechar cadastro' : 'Cadastre-se'}
              </button>
            </div>
          </article>

          {showCadastroForm ? (
            <article className="animal-card perfil-card">
              <h2>Cadastre-se</h2>
              <p className="perfil-subtitle">Crie sua conta para acessar o sistema.</p>

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
            </article>
          ) : null}
        </div>

        <article className="animals-empty perfil-session">
          <p>Sessão atual</p>
          {currentUser ? (
            <span>
              Logado como <strong>{currentUser.nome}</strong> ({currentUser.email}) - perfil{' '}
              {currentUser.perfil}
            </span>
          ) : (
            <span>Faça login para permitir o cadastro de animais.</span>
          )}
        </article>
      </section>
    </main>
  )
}

export default PerfilPage
