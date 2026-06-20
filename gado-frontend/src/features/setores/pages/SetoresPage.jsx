import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import SetorFormModal from '../components/SetorFormModal'
import SetorDetailsModal from '../components/SetorDetailsModal'
import {
  listarSetoresCompletos,
  cadastrarSetor,
  atualizarSetor,
  deletarSetor,
  exportarSetoresCSV,
  exportarSetoresPDF,
} from '../../../services/setorApi'
import { useRefresh } from '../../../contexts/RefreshContext.jsx'
import '../../animais/styles/animais.css'
import '../styles/setores.css'

const PERFIS_COM_EDICAO = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR']

const defaultForm = {
  nome: '',
  capacidadeMaxima: '',
  tipo: '',
  metaTexto: '',
}

const ROWS_PER_PAGE = 10

function SetoresPage({ currentUser, onNavigate, onLogout }) {
  const [search, setSearch] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [setores, setSetores] = useState([])
  const [modal, setModal] = useState({ type: null, setor: null })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')
  const [exportMenuOpen, setExportMenuOpen] = useState(false)
  const exportMenuRef = useRef(null)
  const [page, setPage] = useState(0)

  const { refreshGlobal, dispararRefresh } = useRefresh()

  const canEdit = PERFIS_COM_EDICAO.includes(currentUser?.perfil)

  useEffect(() => {
    if (!exportMenuOpen) return
    function handleClickOutside(event) {
      if (exportMenuRef.current && !exportMenuRef.current.contains(event.target)) {
        setExportMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [exportMenuOpen])

  function handleExportarCSV() {
    exportarSetoresCSV(filteredSetores)
    setExportMenuOpen(false)
  }

  function handleExportarPDF() {
    exportarSetoresPDF()
    setExportMenuOpen(false)
  }

  const filteredSetores = useMemo(() => {
    const termo = activeSearch.toLowerCase()
    if (!termo) return setores
    return setores.filter(
      (s) =>
        s.nome.toLowerCase().includes(termo) ||
        s.tipo.toLowerCase().includes(termo) ||
        s.criadoPorNome.toLowerCase().includes(termo),
    )
  }, [setores, activeSearch])

  const totalPages = Math.max(1, Math.ceil(filteredSetores.length / ROWS_PER_PAGE))
  const paginatedSetores = filteredSetores.slice(
    page * ROWS_PER_PAGE,
    (page + 1) * ROWS_PER_PAGE,
  )

  const fetchSetores = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await listarSetoresCompletos()
      setSetores(list)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar setores.',
      })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchSetores()
  }, [fetchSetores, refreshGlobal])

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

  function closeModal() {
    setModal({ type: null, setor: null })
    setFormData(defaultForm)
    setFormFeedback('')
  }

  function handleFormChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({ ...current, [name]: value }))
  }

  function openCreateModal() {
    setFormMode('create')
    setFormData(defaultForm)
    setFormFeedback('')
    setModal({ type: 'form', setor: null })
  }

  function openEditModal(setor) {
    setFormMode('edit')
    setFormFeedback('')
    setFormData({
      id: setor.id,
      nome: setor.nome,
      capacidadeMaxima: setor.capacidadeMaxima,
      tipo: setor.tipo,
      metaTexto: setor.metaTexto ?? '',
    })
    setModal({ type: 'form', setor })
  }

  function openDetailsModal(setor) {
    setModal({ type: 'details', setor })
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setFormFeedback('')
    setFeedback({ type: '', message: '' })
    setIsSaving(true)
    try {
      if (formMode === 'create') {
        await cadastrarSetor(currentUser.email, formData)
        setFeedback({ type: 'info', message: 'Setor cadastrado com sucesso.' })
      } else {
        await atualizarSetor(modal.setor.id, currentUser.email, formData)
        setFeedback({ type: 'info', message: 'Setor atualizado com sucesso.' })
      }
      dispararRefresh()
      closeModal()
      await fetchSetores()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar o setor.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(setor) {
    const confirmDelete = window.confirm(
      `Deseja excluir o setor "${setor.nome}"?`,
    )
    if (!confirmDelete) return

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })
    try {
      await deletarSetor(setor.id, currentUser.email)
      closeModal()
      setFeedback({ type: 'info', message: 'Setor excluído com sucesso.' })
      await fetchSetores()
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao excluir setor.',
      })
    } finally {
      setIsDeleting(false)
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
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('lotes')}
          >
            Lotes
          </button>
          <button type="button" className="menu-item menu-item--active">
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
          {currentUser.perfil === 'ADMINISTRADOR' ? (
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
          <h1>Setores</h1>
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
              placeholder="Buscar por nome, tipo ou criado por"
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

          <div className="export-wrapper" ref={exportMenuRef}>
            <button
              type="button"
              className="btn-export-csv"
              onClick={() => setExportMenuOpen((v) => !v)}
            >
              Exportar ▾
            </button>
            {exportMenuOpen ? (
              <div className="export-menu">
                <button
                  type="button"
                  className="export-menu__item"
                  onClick={handleExportarCSV}
                >
                  Exportar como CSV
                </button>
                <hr className="export-menu__separator" />
                <button
                  type="button"
                  className="export-menu__item"
                  onClick={handleExportarPDF}
                >
                  Exportar como PDF
                </button>
              </div>
            ) : null}
          </div>

          {canEdit ? (
            <button type="button" className="btn-new-entity" onClick={openCreateModal}>
              + Novo Setor
            </button>
          ) : null}
        </div>

        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Nome</th>
                <th>Tipo</th>
                <th>Cap. Máxima</th>
                <th>Lotes</th>
                <th>Meta</th>
                <th>Criado Por</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="table-loading">
                    Carregando...
                  </td>
                </tr>
              ) : paginatedSetores.length === 0 ? (
                <tr>
                  <td colSpan={7} className="table-empty">
                    {activeSearch
                      ? `Nenhum resultado para "${activeSearch}".`
                      : 'Nenhum setor cadastrado. Clique em "+ Novo Setor" para começar.'}
                  </td>
                </tr>
              ) : (
                paginatedSetores.map((setor) => (
                  <tr key={setor.id}>
                    <td>{setor.nome}</td>
                    <td>{setor.tipo || '—'}</td>
                    <td>{setor.capacidadeMaxima ?? '—'}</td>
                    <td>{setor.lotes.length > 0 ? setor.lotes.length : '—'}</td>
                    <td className="td-truncate">{setor.metaTexto || '—'}</td>
                    <td>{setor.criadoPorNome || '—'}</td>
                    <td>
                      <div className="row-actions">
                        <button
                          type="button"
                          className="btn-row"
                          onClick={() => openDetailsModal(setor)}
                        >
                          Detalhes
                        </button>
                        {canEdit ? (
                          <button
                            type="button"
                            className="btn-row btn-row--edit"
                            onClick={() => openEditModal(setor)}
                          >
                            Editar
                          </button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <footer className="data-pagination">
          <span className="pagination-info">
            {isLoading
              ? ''
              : `${filteredSetores.length} ${filteredSetores.length === 1 ? 'registro' : 'registros'}`}
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

      {modal.type === 'form' ? (
        <SetorFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          feedback={formFeedback}
          currentUser={currentUser}
          onClose={closeModal}
          onChange={handleFormChange}
          onSubmit={handleSubmitForm}
        />
      ) : null}

      {modal.type === 'details' && modal.setor ? (
        <SetorDetailsModal
          setor={modal.setor}
          onClose={closeModal}
          onEdit={() => openEditModal(modal.setor)}
          onDelete={handleDelete}
          isDeleting={isDeleting}
        />
      ) : null}
    </main>
  )
}

export default SetoresPage
