import { useCallback, useEffect, useState } from 'react'
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

const PERFIL_LABELS = {
  ADMINISTRADOR: 'Administrador',
  GERENTE: 'Gerente',
  CUIDADOR: 'Cuidador',
}

const ROWS_PER_PAGE = 10

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
  const [filterPerfil, setFilterPerfil] = useState('')
  const [page, setPage] = useState(0)

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
    setPage(0)
  }

  function handleClearSearch() {
    setSearch('')
    setActiveSearch('')
    setPage(0)
  }

  const usuariosFiltrados = usuarios.filter((u) => {
    if (activeSearch) {
      const termo = activeSearch.toLowerCase()
      const matchNome = String(u.nome ?? '').toLowerCase().includes(termo)
      const matchEmail = String(u.email ?? '').toLowerCase().includes(termo)
      if (!matchNome && !matchEmail) return false
    }
    if (filterPerfil && u.perfil !== filterPerfil) return false
    return true
  })

  const totalPages = Math.max(1, Math.ceil(usuariosFiltrados.length / ROWS_PER_PAGE))
  const paginatedUsuarios = usuariosFiltrados.slice(
    page * ROWS_PER_PAGE,
    (page + 1) * ROWS_PER_PAGE,
  )

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

  function perfilPillClass(perfil) {
    if (perfil === 'ADMINISTRADOR') return 'perfil-pill perfil-pill--admin'
    if (perfil === 'GERENTE') return 'perfil-pill perfil-pill--gerente'
    return 'perfil-pill perfil-pill--cuidador'
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
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('lotes')}
          >
            Lotes
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('setores')}
          >
            Setores
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('metas')}
          >
            Metas
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
        <header className="page-header">
          <h1>Configurações</h1>
        </header>

        {feedback.message ? (
          <p
            className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
          >
            {feedback.message}
          </p>
        ) : null}

        <div className="data-toolbar">
          <form className="toolbar-search" onSubmit={handleSearchSubmit}>
            <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Buscar por nome ou e-mail"
            />
            {activeSearch ? (
              <button
                type="button"
                className="toolbar-search__clear"
                onClick={handleClearSearch}
                aria-label="Limpar busca"
              >
                ✕
              </button>
            ) : null}
          </form>

          <select
            className="toolbar-select"
            value={filterPerfil}
            onChange={(e) => {
              setFilterPerfil(e.target.value)
              setPage(0)
            }}
          >
            <option value="">Todos os Perfis</option>
            <option value="ADMINISTRADOR">Administrador</option>
            <option value="GERENTE">Gerente</option>
            <option value="CUIDADOR">Cuidador</option>
          </select>

          <button type="button" className="btn-new-entity" onClick={openCreateModal}>
            + Novo Usuário
          </button>
        </div>

        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Perfil</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={4} className="table-loading">
                    Carregando...
                  </td>
                </tr>
              ) : paginatedUsuarios.length === 0 ? (
                <tr>
                  <td colSpan={4} className="table-empty">
                    {activeSearch
                      ? `Nenhum resultado para "${activeSearch}".`
                      : 'Nenhum usuário cadastrado. Clique em "+ Novo Usuário" para começar.'}
                  </td>
                </tr>
              ) : (
                paginatedUsuarios.map((usuario) => {
                  const isCurrentUser = usuario.email === currentUser.email
                  return (
                    <tr key={usuario.email}>
                      <td>
                        {usuario.nome}
                        {isCurrentUser ? (
                          <span className="current-user-badge"> (você)</span>
                        ) : null}
                      </td>
                      <td className="td-mono">{usuario.email}</td>
                      <td>
                        <span className={perfilPillClass(usuario.perfil)}>
                          {PERFIL_LABELS[usuario.perfil] ?? usuario.perfil}
                        </span>
                      </td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="btn-row btn-row--danger"
                            onClick={() => handleExcluir(usuario)}
                            disabled={isCurrentUser}
                            title={
                              isCurrentUser
                                ? 'Não é possível excluir sua própria conta'
                                : undefined
                            }
                          >
                            Excluir
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>

        <footer className="data-pagination">
          <span className="pagination-info">
            {isLoading
              ? ''
              : `${usuariosFiltrados.length} ${usuariosFiltrados.length === 1 ? 'registro' : 'registros'}`}
          </span>
          <div className="pagination-controls">
            <button
              type="button"
              className="pagination-btn"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
            >
              ← Anterior
            </button>
            <span className="pagination-pages">
              Página {page + 1} de {totalPages}
            </span>
            <button
              type="button"
              className="pagination-btn"
              disabled={page >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Próximo →
            </button>
          </div>
        </footer>
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
