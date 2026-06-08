import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import SetorCard from '../components/SetorCard'
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
  }

  function handleClearSearch() {
    setSearch('')
    setActiveSearch('')
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
        <header className="animals-header">
          <h1>Setores</h1>
          <span>{currentUser.email}</span>
        </header>

        <div className="search-toolbar">
          <form className="animals-search" onSubmit={handleSearchSubmit}>
            <input
              type="text"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Buscar por nome, tipo ou criado por"
            />
            <button type="submit" disabled={isLoading}>
              {isLoading ? 'Buscando...' : 'Buscar'}
            </button>
            {activeSearch ? (
              <button
                type="button"
                onClick={handleClearSearch}
                disabled={isLoading}
              >
                Limpar
              </button>
            ) : null}
          </form>

          <div className="export-wrapper" ref={exportMenuRef}>
            <button
              type="button"
              className="export-btn"
              onClick={() => setExportMenuOpen((v) => !v)}
            >
              Exportar ▾
            </button>
            {exportMenuOpen && (
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
            )}
          </div>
        </div>

        <p className="animals-count">
          {isLoading
            ? 'Carregando...'
            : activeSearch
              ? `${filteredSetores.length} ${filteredSetores.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
              : `${filteredSetores.length} ${filteredSetores.length === 1 ? 'setor cadastrado' : 'setores cadastrados'}`}
        </p>

        {feedback.message ? (
          <p
            className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
          >
            {feedback.message}
          </p>
        ) : null}

        {filteredSetores.length ? (
          <div className="animals-grid">
            {filteredSetores.map((setor) => (
              <SetorCard
                key={setor.id}
                setor={setor}
                onDetalhes={openDetailsModal}
                onEditar={openEditModal}
              />
            ))}
          </div>
        ) : (
          <div className="animals-empty">
            {activeSearch ? (
              <>
                <p>Nenhum setor encontrado.</p>
                <span>
                  Nenhum resultado para {`"${activeSearch}"`}. Ajuste o termo
                  da busca.
                </span>
              </>
            ) : (
              <>
                <p>Nenhum setor cadastrado.</p>
                <span>Clique no botão + para cadastrar o primeiro setor.</span>
              </>
            )}
          </div>
        )}

        {canEdit ? (
          <button
            type="button"
            className="fab-add"
            aria-label="Adicionar setor"
            onClick={openCreateModal}
          >
            +
          </button>
        ) : null}
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
