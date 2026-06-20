import { useState } from 'react'
import { atualizarUsuario } from '../../../services/usuarioApi'
import '../../animais/styles/animais.css'
import '../styles/perfil.css'

const PERFIL_OPTIONS = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR', 'FINANCEIRO']

function PerfilPage({ currentUser, onLogout, onNavigate, onUpdateUser }) {
  const [editMode, setEditMode] = useState(false)
  const [editForm, setEditForm] = useState({ nome: currentUser.nome, perfil: currentUser.perfil })
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

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

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo">🌿</div>
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
                    <dd>{currentUser.perfil}</dd>
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
                  <input type="email" value={currentUser.email} disabled className="perfil-disabled-input" />
                </label>

                {isAdmin ? (
                  <label>
                    <span>Perfil</span>
                    <select name="perfil" value={editForm.perfil} onChange={handleEditChange}>
                      {PERFIL_OPTIONS.map((p) => (
                        <option key={p} value={p}>
                          {p}
                        </option>
                      ))}
                    </select>
                  </label>
                ) : (
                  <label>
                    <span>Perfil</span>
                    <input type="text" value={currentUser.perfil} disabled className="perfil-disabled-input" />
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
        </div>
      </section>
    </main>
  )
}

export default PerfilPage
