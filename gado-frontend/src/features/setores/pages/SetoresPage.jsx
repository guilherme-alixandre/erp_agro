import { useCallback, useEffect, useState } from 'react'
import SetorCard from '../components/SetorCard'
import SetorFormModal from '../components/SetorFormModal'
import SetorDetailsModal from '../components/SetorDetailsModal'
import {
  listarSetores,
  cadastrarSetor,
  atualizarSetor,
  deletarSetor,
  exportarSetoresCSV,
  exportarSetoresPDF,
} from '../integration/setorApi'
import '../../animais/styles/animais.css'
import '../styles/setores.css'

const PERFIS_COM_CRIACAO_EDICAO_SETOR = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR_CHEFE']
const PERFIS_COM_EXCLUSAO_SETOR = ['ADMINISTRADOR', 'GERENTE']

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

  const canCreateSetor = PERFIS_COM_CRIACAO_EDICAO_SETOR.includes(currentUser?.perfil)
  const canEditSetor = PERFIS_COM_CRIACAO_EDICAO_SETOR.includes(currentUser?.perfil)
  const canDeleteSetor = PERFIS_COM_EXCLUSAO_SETOR.includes(currentUser?.perfil)

  const fetchSetores = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await listarSetores()
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
  }, [fetchSetores])

  const setoresFiltrados = activeSearch
    ? setores.filter((s) => {
        const termo = activeSearch.toLowerCase()
        return (
          s.nome.toLowerCase().includes(termo) ||
          s.tipo.toLowerCase().includes(termo) ||
          (s.criadoPorNome ?? '').toLowerCase().includes(termo)
        )
      })
    : setores

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
      closeModal()
      await fetchSetores()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar o setor.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(setor) {
    if (!window.confirm(`Deseja excluir o setor "${setor.nome}"?`)) return

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })
    try {
      await deletarSetor(setor.id)
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
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('lotes')}>
            Lotes
          </button>
          <button type="button" className="menu-item menu-item--active">
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
          <button type="button" className="menu-item" onClick={() => onNavigate('perfil')}>
            Perfil
          </button>
          {currentUser.perfil === 'ADMINISTRADOR' && (
            <button type="button" className="menu-item" onClick={() => onNavigate('configuracoes')}>
              ⚙ Configurações
            </button>
          )}
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

        <div className="setores-export">
          <button type="button" onClick={() => exportarSetoresCSV(setoresFiltrados)}>
            Exportar CSV
          </button>
          <button type="button" onClick={exportarSetoresPDF}>
            Exportar PDF
          </button>
        </div>

        <form className="animals-search" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por nome, tipo ou criado por"
          />
          <button type="submit" disabled={isLoading}>
            Buscar
          </button>
          {activeSearch && (
            <button type="button" onClick={handleClearSearch} disabled={isLoading}>
              Limpar
            </button>
          )}
        </form>

        <p className="animals-count">
          {isLoading
            ? 'Carregando...'
            : activeSearch
              ? `${setoresFiltrados.length} ${setoresFiltrados.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
              : `${setores.length} ${setores.length === 1 ? 'setor cadastrado' : 'setores cadastrados'}`}
        </p>

        {feedback.message && (
          <p className={`feedback feedback--${feedback.type === 'error' ? 'error' : 'info'}`}>
            {feedback.message}
          </p>
        )}

        {setoresFiltrados.length ? (
          <div className="animals-grid">
            {setoresFiltrados.map((setor) => (
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
                <span>Nenhum resultado para "{activeSearch}". Ajuste o termo da busca.</span>
              </>
            ) : (
              <>
                <p>Nenhum setor cadastrado.</p>
                <span>Clique no botão + para cadastrar o primeiro setor.</span>
              </>
            )}
          </div>
        )}

        {canCreateSetor && (
          <button type="button" className="fab-add" aria-label="Adicionar setor" onClick={openCreateModal}>
            +
          </button>
        )}
      </section>

      {modal.type === 'form' && (
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
      )}

      {modal.type === 'details' && modal.setor && (
        <SetorDetailsModal
          setor={modal.setor}
          onClose={closeModal}
          onEdit={() => openEditModal(modal.setor)}
          onDelete={handleDelete}
          isDeleting={isDeleting}
          canEdit={canEditSetor}
          canDelete={canDeleteSetor}
        />
      )}
    </main>
  )
}

export default SetoresPage
