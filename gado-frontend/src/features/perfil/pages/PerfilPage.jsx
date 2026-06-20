import { useState } from 'react'
import { atualizarUsuario, verificarCredenciais } from '../../../services/usuarioApi'
import '../../animais/styles/animais.css'
import '../styles/perfil.css'

const PERFIL_OPTIONS = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR', 'CUIDADOR_CHEFE', 'FINANCEIRO']

const PERFIL_LABELS = {
  ADMINISTRADOR: 'Administrador',
  GERENTE: 'Gerente',
  CUIDADOR: 'Cuidador',
  CUIDADOR_CHEFE: 'Cuidador Chefe',
  FINANCEIRO: 'Financeiro',
}

function PerfilPage({ currentUser, onLogout, onNavigate, onUpdateUser }) {
  const [editMode, setEditMode] = useState(false)
  const [editForm, setEditForm] = useState({ nome: currentUser.nome, perfil: currentUser.perfil })
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [senhaMode, setSenhaMode] = useState(false)
  const [senhaForm, setSenhaForm] = useState({ senhaAtual: '', novaSenha: '', confirmar: '' })
  const [isSavingSenha, setIsSavingSenha] = useState(false)
  const [senhaError, setSenhaError] = useState('')
  const [showSenhaAtual, setShowSenhaAtual] = useState(false)
  const [showNovaSenha, setShowNovaSenha] = useState(false)
  const [showConfirmar, setShowConfirmar] = useState(false)

  const isAdmin = currentUser.perfil === 'ADMINISTRADOR'

  function handleEditChange(e) {
    const { name, value } = e.target
    setEditForm((f) => ({ ...f, [name]: value }))
  }

  function openEdit() {
    setEditForm({ nome: currentUser.nome, perfil: currentUser.perfil })
    setFeedback({ type: '', message: '' })
    setEditMode(true)
  }

  function cancelEdit() {
    setEditMode(false)
    setFeedback({ type: '', message: '' })
  }

  async function handleSaveEdit(e) {
    e.preventDefault()
    if (!editForm.nome?.trim()) return
    setIsSaving(true)
    setFeedback({ type: '', message: '' })
    try {
      const updated = await atualizarUsuario(currentUser.email, {
        nome: editForm.nome.trim(),
        ...(isAdmin ? { perfil: editForm.perfil } : {}),
      })
      onUpdateUser(updated)
      setFeedback({ type: 'info', message: 'Perfil atualizado com sucesso.' })
      setEditMode(false)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao atualizar perfil.' })
    } finally {
      setIsSaving(false)
    }
  }

  function handleSenhaChange(e) {
    const { name, value } = e.target
    setSenhaForm((f) => ({ ...f, [name]: value }))
    setSenhaError('')
  }

  function openSenha() {
    setSenhaForm({ senhaAtual: '', novaSenha: '', confirmar: '' })
    setSenhaError('')
    setFeedback({ type: '', message: '' })
    setSenhaMode(true)
  }

  function cancelSenha() {
    setSenhaMode(false)
    setSenhaError('')
  }

  async function handleSaveSenha(e) {
    e.preventDefault()
    if (senhaForm.novaSenha.length < 4) {
      setSenhaError('A nova senha deve ter ao menos 4 caracteres.')
      return
    }
    if (senhaForm.novaSenha !== senhaForm.confirmar) {
      setSenhaError('A nova senha e a confirmação não coincidem.')
      return
    }
    setIsSavingSenha(true)
    setSenhaError('')
    try {
      await verificarCredenciais(currentUser.email, senhaForm.senhaAtual)
      await atualizarUsuario(currentUser.email, { senha: senhaForm.novaSenha })
      setFeedback({ type: 'info', message: 'Senha alterada com sucesso.' })
      setSenhaMode(false)
      setSenhaForm({ senhaAtual: '', novaSenha: '', confirmar: '' })
    } catch (error) {
      const msg = String(error?.message ?? '').toLowerCase()
      if (msg.includes('credenciais') || msg.includes('inválid') || msg.includes('invalid')) {
        setSenhaError('Senha atual incorreta.')
      } else {
        setSenhaError(error.message || 'Falha ao alterar a senha.')
      }
    } finally {
      setIsSavingSenha(false)
    }
  }

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo"><img src="/logo.png" alt="GADO" /></div>
        <nav>
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('lotes')}>
            Lotes
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('setores')}>
            Setores
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('metas')}>
            Metas
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('insumos')}>
            Insumos
          </button>
          <button type="button" className="menu-item">
            Financeiro
          </button>
          <button type="button" className="menu-item menu-item--active">
            Perfil
          </button>
          {isAdmin ? (
            <button
              type="button"
              className="menu-item"
              onClick={() => onNavigate('configuracoes')}
            >
              ⚙ Configurações
            </button>
          ) : null}
        </nav>
        <div className="sidebar-user">
          <strong>{currentUser.nome}</strong>
          <span>{currentUser.email}</span>
          <button type="button" className="sidebar-logout" onClick={onLogout}>
            Sair
          </button>
        </div>
      </aside>

      <section className="animals-content">
        <header className="page-header">
          <h1>Perfil</h1>
        </header>

        {feedback.message ? (
          <p
            className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
          >
            {feedback.message}
          </p>
        ) : null}

        <div className="perfil-stack">
          {/* ── Card: Dados da conta ── */}
          <article className="animal-card perfil-card perfil-card--main">
            <h2>Dados da conta</h2>
            <p className="perfil-subtitle">Informações da sessão atual.</p>

            {!editMode ? (
              <>
                <dl className="perfil-info">
                  <div>
                    <dt>Nome</dt>
                    <dd>{currentUser.nome}</dd>
                  </div>
                  <div>
                    <dt>E-mail</dt>
                    <dd>{currentUser.email}</dd>
                  </div>
                  <div>
                    <dt>Perfil</dt>
                    <dd>{PERFIL_LABELS[currentUser.perfil] ?? currentUser.perfil}</dd>
                  </div>
                </dl>

                <div className="modal-actions perfil-actions">
                  <button type="button" className="btn-secondary" onClick={onLogout}>
                    Sair
                  </button>
                  <button type="button" className="btn-primary" onClick={openEdit}>
                    Editar perfil
                  </button>
                </div>
              </>
            ) : (
              <form className="animal-form perfil-edit-form" onSubmit={handleSaveEdit}>
                <label>
                  <span>
                    Nome <span className="required-marker" aria-hidden="true">*</span>
                  </span>
                  <input
                    type="text"
                    name="nome"
                    value={editForm.nome}
                    onChange={handleEditChange}
                    required
                    autoFocus
                  />
                </label>

                <label>
                  <span>E-mail</span>
                  <input
                    type="email"
                    value={currentUser.email}
                    disabled
                    className="perfil-disabled-input"
                  />
                </label>

                {isAdmin ? (
                  <label>
                    <span>Perfil</span>
                    <select name="perfil" value={editForm.perfil} onChange={handleEditChange}>
                      {PERFIL_OPTIONS.map((p) => (
                        <option key={p} value={p}>
                          {PERFIL_LABELS[p] ?? p}
                        </option>
                      ))}
                    </select>
                  </label>
                ) : (
                  <label>
                    <span>Perfil</span>
                    <input
                      type="text"
                      value={PERFIL_LABELS[currentUser.perfil] ?? currentUser.perfil}
                      disabled
                      className="perfil-disabled-input"
                    />
                  </label>
                )}

                <div className="modal-actions">
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={cancelEdit}
                    disabled={isSaving}
                  >
                    Cancelar
                  </button>
                  <button type="submit" className="btn-primary" disabled={isSaving}>
                    {isSaving ? 'Salvando...' : 'Salvar'}
                  </button>
                </div>
              </form>
            )}
          </article>

          {/* ── Card: Alterar senha ── */}
          <article className="animal-card perfil-card">
            <h2>Alterar senha</h2>
            <p className="perfil-subtitle">Redefina a senha de acesso à sua conta.</p>

            {!senhaMode ? (
              <div className="modal-actions perfil-actions">
                <button type="button" className="btn-primary" onClick={openSenha}>
                  Alterar senha
                </button>
              </div>
            ) : (
              <form className="animal-form perfil-edit-form" onSubmit={handleSaveSenha}>
                <label>
                  <span>
                    Senha atual <span className="required-marker" aria-hidden="true">*</span>
                  </span>
                  <div className="password-field">
                    <input
                      type={showSenhaAtual ? 'text' : 'password'}
                      name="senhaAtual"
                      value={senhaForm.senhaAtual}
                      onChange={handleSenhaChange}
                      required
                      autoFocus
                      autoComplete="current-password"
                    />
                    <button
                      type="button"
                      className="password-field__toggle"
                      onClick={() => setShowSenhaAtual((v) => !v)}
                      aria-label={showSenhaAtual ? 'Ocultar senha atual' : 'Mostrar senha atual'}
                    >
                      {showSenhaAtual ? 'Ocultar' : 'Exibir'}
                    </button>
                  </div>
                </label>

                <label>
                  <span>
                    Nova senha <span className="required-marker" aria-hidden="true">*</span>
                  </span>
                  <div className="password-field">
                    <input
                      type={showNovaSenha ? 'text' : 'password'}
                      name="novaSenha"
                      value={senhaForm.novaSenha}
                      onChange={handleSenhaChange}
                      required
                      minLength={4}
                      placeholder="Mínimo 4 caracteres"
                      autoComplete="new-password"
                    />
                    <button
                      type="button"
                      className="password-field__toggle"
                      onClick={() => setShowNovaSenha((v) => !v)}
                      aria-label={showNovaSenha ? 'Ocultar nova senha' : 'Mostrar nova senha'}
                    >
                      {showNovaSenha ? 'Ocultar' : 'Exibir'}
                    </button>
                  </div>
                </label>

                <label>
                  <span>
                    Confirmar nova senha{' '}
                    <span className="required-marker" aria-hidden="true">*</span>
                  </span>
                  <div className="password-field">
                    <input
                      type={showConfirmar ? 'text' : 'password'}
                      name="confirmar"
                      value={senhaForm.confirmar}
                      onChange={handleSenhaChange}
                      required
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

                <div className="modal-actions">
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={cancelSenha}
                    disabled={isSavingSenha}
                  >
                    Cancelar
                  </button>
                  <button type="submit" className="btn-primary" disabled={isSavingSenha}>
                    {isSavingSenha ? 'Salvando...' : 'Confirmar alteração'}
                  </button>
                </div>
              </form>
            )}
          </article>
        </div>
      </section>
    </main>
  )
}

export default PerfilPage
