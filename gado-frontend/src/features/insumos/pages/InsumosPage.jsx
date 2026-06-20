import { useCallback, useEffect, useState } from 'react'
import VacinaCard from '../components/VacinaCard'
import VacinaFormModal from '../components/VacinaFormModal'
import {
  atualizarVacina,
  cadastrarVacina,
  confirmarVacina,
  deletarVacina,
  listarVacinas,
} from '../../../services/insumoApi'
import { useRefresh } from '../../../contexts/RefreshContext.jsx'
import '../../animais/styles/animais.css'
import '../styles/insumos.css'

const defaultForm = {
  id: null,
  nome: '',
  pendente: false,
}

function InsumosPage({ currentUser, onNavigate, onLogout }) {
  const { refreshGlobal, dispararRefresh } = useRefresh()

  const [activeTab, setActiveTab] = useState('vacinas')
  const [search, setSearch] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [vacinas, setVacinas] = useState([])
  const [modal, setModal] = useState({ open: false })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')

  const fetchVacinas = useCallback(async (termo) => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await listarVacinas(termo)
      setVacinas(list)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar vacinas.',
      })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchVacinas('')
  }, [fetchVacinas, refreshGlobal])

  function handleSearchSubmit(event) {
    event.preventDefault()
    const termo = search.trim()
    setActiveSearch(termo)
    fetchVacinas(termo)
  }

  function handleClearSearch() {
    setSearch('')
    setActiveSearch('')
    fetchVacinas('')
  }

  function closeModal() {
    setModal({ open: false })
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
    setModal({ open: true })
  }

  function openEditModal(vacina) {
    setFormMode('edit')
    setFormFeedback('')
    setFormData({
      id: vacina.id,
      nome: vacina.nome,
      pendente: vacina.pendente === true,
    })
    setModal({ open: true })
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    try {
      if (formMode === 'create') {
        await cadastrarVacina({ nome: formData.nome, pendente: false })
        setFeedback({ type: 'info', message: 'Vacina cadastrada com sucesso.' })
      } else {
        await atualizarVacina(formData.id, { nome: formData.nome })
        setFeedback({ type: 'info', message: 'Vacina atualizada com sucesso.' })
      }
      dispararRefresh()
      closeModal()
      await fetchVacinas(activeSearch)
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar vacina.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleConfirmar() {
    if (!formData.id) return
    setIsSaving(true)
    setFormFeedback('')
    try {
      await confirmarVacina(formData.id)
      setFeedback({
        type: 'info',
        message: 'Vacina confirmada com sucesso.',
      })
      closeModal()
      await fetchVacinas(activeSearch)
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao confirmar vacina.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete() {
    if (!formData.id) return
    const confirmDelete = window.confirm(
      `Deseja excluir a vacina "${formData.nome}"?`,
    )
    if (!confirmDelete) return

    setIsDeleting(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })
    try {
      await deletarVacina(formData.id)
      setFeedback({ type: 'info', message: 'Vacina excluída com sucesso.' })
      closeModal()
      await fetchVacinas(activeSearch)
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao excluir vacina.')
    } finally {
      setIsDeleting(false)
    }
  }

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo"><img src="/logo.png" alt="GADO" /></div>
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
          <button type="button" className="menu-item menu-item--active">
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
          <h1>Insumos</h1>
          <span>{currentUser.email}</span>
        </header>

        <div className="insumos-tabs">
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'vacinas' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('vacinas')}
          >
            Vacinas
          </button>
          <button
            type="button"
            className="insumos-tab"
            disabled
            title="Em breve"
          >
            Insumos (em breve)
          </button>
        </div>

        {activeTab === 'vacinas' ? (
          <>
            <form className="animals-search" onSubmit={handleSearchSubmit}>
              <input
                type="text"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Buscar vacina por nome"
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

            <p className="animals-count">
              {isLoading
                ? 'Carregando...'
                : activeSearch
                  ? `${vacinas.length} ${vacinas.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
                  : `${vacinas.length} ${vacinas.length === 1 ? 'vacina cadastrada' : 'vacinas cadastradas'}`}
            </p>

            {feedback.message ? (
              <p
                className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
              >
                {feedback.message}
              </p>
            ) : null}

            {vacinas.length ? (
              <div className="animals-grid">
                {vacinas.map((vacina) => (
                  <VacinaCard
                    key={vacina.id}
                    vacina={vacina}
                    onEditar={openEditModal}
                  />
                ))}
              </div>
            ) : (
              <div className="animals-empty">
                {activeSearch ? (
                  <>
                    <p>Nenhuma vacina encontrada.</p>
                    <span>
                      Nenhum resultado para {`"${activeSearch}"`}. Ajuste a busca.
                    </span>
                  </>
                ) : (
                  <>
                    <p>Nenhuma vacina cadastrada.</p>
                    <span>Clique no botão + para cadastrar a primeira vacina.</span>
                  </>
                )}
              </div>
            )}

            <button
              type="button"
              className="fab-add"
              aria-label="Adicionar vacina"
              onClick={openCreateModal}
            >
              +
            </button>
          </>
        ) : (
          <div className="insumos-placeholder">
            <p>Em breve</p>
            <span>Cadastro geral de insumos será habilitado em uma próxima versão.</span>
          </div>
        )}
      </section>

      {modal.open ? (
        <VacinaFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          isDeleting={isDeleting}
          feedback={formFeedback}
          onClose={closeModal}
          onChange={handleFormChange}
          onSubmit={handleSubmitForm}
          onConfirmar={handleConfirmar}
          onDelete={handleDelete}
        />
      ) : null}
    </main>
  )
}

export default InsumosPage
