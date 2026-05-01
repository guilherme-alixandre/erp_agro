import { useCallback, useEffect, useState } from 'react'
import UsuarioCard from '../components/UsuarioCard'
import UsuarioFormModal from '../components/UsuarioFormModal'
import {
  cadastrarUsuario,
  deletarUsuario,
  listarUsuarios,
} from '../../../services/usuarioApi'
import '../../animais/styles/animais.css'
import '../styles/configuracoes.css'

const defaultForm = {
  nome: '',
  email: '',
  senha: '',
  perfil: 'GERENTE',
}

function ConfiguracoesPage({ currentUser, onNavigate, onLogout }) {
  const [search, setSearch] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [usuarios, setUsuarios] = useState([])
  const [modalOpen, setModalOpen] = useState(false)
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')

  const fetchUsuarios = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await listarUsuarios(currentUser.email)
      setUsuarios(list)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar usuários.',
      })
    } finally {
      setIsLoading(false)
    }
  }, [currentUser.email])

  useEffect(() => {
    fetchUsuarios()
  }, [fetchUsuarios])

  function handleSearchSubmit(event) {
    event.preventDefault()
    setActiveSearch(search.trim())
  }

  function handleClearSearch() {
    setSearch('')
    setActiveSearch('')
  }

  const usuariosFiltrados = activeSearch
    ? usuarios.filter((u) => {
        const termo = activeSearch.toLowerCase()
        return (
          String(u.nome ?? '').toLowerCase().includes(termo) ||
          String(u.email ?? '').toLowerCase().includes(termo)
        )
      })
    : usuarios

  function openCreateModal() {
    setFormData(defaultForm)
    setFormFeedback('')
    setModalOpen(true)
  }

  function closeModal() {
    setModalOpen(false)
    setFormData(defaultForm)
    setFormFeedback('')
  }

  function handleFormChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({ ...current, [name]: value }))
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    try {
      await cadastrarUsuario(formData, currentUser.email)
      setFeedback({
        type: 'info',
        message: `Usuário ${formData.nome} cadastrado com sucesso.`,
      })
      closeModal()
      await fetchUsuarios()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao cadastrar usuário.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleExcluir(usuario) {
    const confirmar = window.confirm(
      `Deseja excluir o usuário ${usuario.nome} (${usuario.email})?`,
    )
    if (!confirmar) return

    setFeedback({ type: '', message: '' })
    try {
      await deletarUsuario(usuario.email, currentUser.email)
      setFeedback({ type: 'info', message: 'Usuário excluído com sucesso.' })
      await fetchUsuarios()
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao excluir usuário.',
      })
    }
  }

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo">🌿</div>
        <nav>
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('animais')}
          >
            Animais
          </button>
          <button type="button" className="menu-item">
            Lotes
          </button>
          <button type="button" className="menu-item">
            Setores
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('insumos')}
          >
            Insumos
          </button>
          <button type="button" className="menu-item">
            Financeiro
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('perfil')}
          >
            Perfil
          </button>
          <button type="button" className="menu-item menu-item--active">
            ⚙ Configurações
          </button>
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
        <header className="animals-header">
          <h1>Configurações</h1>
          <span>{currentUser.email}</span>
        </header>

        <h2 style={{ margin: '8px 0 6px' }}>Usuários</h2>

        <form className="animals-search" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Buscar por nome ou e-mail"
          />
          <button type="submit" disabled={isLoading}>
            Buscar
          </button>
          {activeSearch ? (
            <button type="button" onClick={handleClearSearch}>
              Limpar
            </button>
          ) : null}
        </form>

        <p className="animals-count">
          {isLoading
            ? 'Carregando...'
            : activeSearch
              ? `${usuariosFiltrados.length} ${usuariosFiltrados.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
              : `${usuarios.length} ${usuarios.length === 1 ? 'usuário cadastrado' : 'usuários cadastrados'}`}
        </p>

        {feedback.message ? (
          <p
            className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
          >
            {feedback.message}
          </p>
        ) : null}

        {usuariosFiltrados.length ? (
          <div className="animals-grid">
            {usuariosFiltrados.map((usuario) => (
              <UsuarioCard
                key={usuario.email}
                usuario={usuario}
                onExcluir={handleExcluir}
                isCurrentUser={usuario.email === currentUser.email}
              />
            ))}
          </div>
        ) : (
          <div className="animals-empty">
            {activeSearch ? (
              <>
                <p>Nenhum usuário encontrado.</p>
                <span>Nenhum resultado para "{activeSearch}".</span>
              </>
            ) : (
              <>
                <p>Nenhum usuário cadastrado.</p>
                <span>Clique no botão + para cadastrar um usuário.</span>
              </>
            )}
          </div>
        )}

        <button
          type="button"
          className="fab-add"
          aria-label="Cadastrar usuário"
          onClick={openCreateModal}
        >
          +
        </button>
      </section>

      {modalOpen ? (
        <UsuarioFormModal
          formData={formData}
          isSaving={isSaving}
          feedback={formFeedback}
          onClose={closeModal}
          onChange={handleFormChange}
          onSubmit={handleSubmitForm}
        />
      ) : null}
    </main>
  )
}

export default ConfiguracoesPage
