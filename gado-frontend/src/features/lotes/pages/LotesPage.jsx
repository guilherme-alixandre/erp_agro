import { useCallback, useEffect, useMemo, useState } from 'react'
import LoteCard from '../components/LoteCard'
import LoteFormModal from '../components/LoteFormModal'
import LoteDetailsModal from '../components/LoteDetailsModal'
import {
  listarLotesCompletos,
  listarAnimaisParaLote,
  cadastrarLote,
  atualizarLote,
  deletarLote,
} from '../../../services/loteApi'
import '../../animais/styles/animais.css'
import '../styles/lotes.css'

const PERFIS_COM_EDICAO = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR']

const defaultForm = {
  codigo: '',
  corBrinco: '',
  descricao: '',
  racaPredominante: '',
  dataCriacao: '',
  alocacoes: [],
}

function LotesPage({ currentUser, setores, onNavigate, onLogout }) {
  const [search, setSearch] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [lotes, setLotes] = useState([])
  const [modal, setModal] = useState({ type: null, lote: null })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')
  const [animaisDisponiveis, setAnimaisDisponiveis] = useState([])

  const canEdit = PERFIS_COM_EDICAO.includes(currentUser?.perfil)

  const filteredLotes = useMemo(() => {
    const termo = activeSearch.toLowerCase()
    if (!termo) return lotes
    return lotes.filter(
      (l) =>
        l.codigo.toLowerCase().includes(termo) ||
        l.corBrinco.toLowerCase().includes(termo) ||
        l.criadoPorNome.toLowerCase().includes(termo),
    )
  }, [lotes, activeSearch])

  const fetchLotes = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await listarLotesCompletos()
      setLotes(list)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar lotes.',
      })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchLotes()
  }, [fetchLotes])

  async function carregarAnimaisDisponiveis() {
    try {
      const lista = await listarAnimaisParaLote()
      setAnimaisDisponiveis(lista)
    } catch {
      setAnimaisDisponiveis([])
    }
  }

  function handleSearchSubmit(event) {
    event.preventDefault()
    setActiveSearch(search.trim())
  }

  function handleClearSearch() {
    setSearch('')
    setActiveSearch('')
  }

  function closeModal() {
    setModal({ type: null, lote: null })
    setFormData(defaultForm)
    setFormFeedback('')
  }

  function handleFormChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({ ...current, [name]: value }))
  }

  function handleAlocacoesChange(newAlocacoes) {
    setFormData((current) => ({ ...current, alocacoes: newAlocacoes }))
  }

  function openCreateModal() {
    setFormMode('create')
    setFormData(defaultForm)
    setFormFeedback('')
    setModal({ type: 'form', lote: null })
    carregarAnimaisDisponiveis()
  }

  function openEditModal(lote) {
    setFormMode('edit')
    setFormFeedback('')
    setFormData({
      ...defaultForm,
      codigo: lote.codigo,
      corBrinco: lote.corBrinco,
      descricao: lote.descricao,
      racaPredominante: lote.racaPredominante,
      dataCriacao: lote.dataCriacao,
      alocacoes: lote.alocacoes.map((aloc) => ({
        setorId: aloc.setorId,
        animaisIds: aloc.animais.map((a) => a.id),
      })),
    })
    setModal({ type: 'form', lote })
    carregarAnimaisDisponiveis()
  }

  function openDetailsModal(lote) {
    setModal({ type: 'details', lote })
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    // Validação: cada setor selecionado deve ter pelo menos um animal
    for (const aloc of formData.alocacoes) {
      if (!Array.isArray(aloc.animaisIds) || aloc.animaisIds.length === 0) {
        const setor = setores.find((s) => s.id === aloc.setorId)
        const nome = setor?.nome ?? `setor ${aloc.setorId}`
        setFormFeedback(
          `O setor "${nome}" não tem animais selecionados. Adicione pelo menos um animal ou remova o setor.`,
        )
        return
      }
    }

    setIsSaving(true)
    try {
      if (formMode === 'create') {
        await cadastrarLote(currentUser.email, formData)
        setFeedback({ type: 'info', message: 'Lote cadastrado com sucesso.' })
      } else {
        await atualizarLote(modal.lote.id, currentUser.email, formData)
        setFeedback({ type: 'info', message: 'Lote atualizado com sucesso.' })
      }
      closeModal()
      await fetchLotes()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar o lote.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(lote) {
    const confirmDelete = window.confirm(`Deseja excluir o lote ${lote.codigo}?`)
    if (!confirmDelete) return

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })
    try {
      await deletarLote(lote.id, currentUser.email)
      closeModal()
      setFeedback({ type: 'info', message: 'Lote excluído com sucesso.' })
      await fetchLotes()
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao excluir lote.',
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
          <button type="button" className="menu-item menu-item--active">
            Lotes
          </button>
          <button type="button" className="menu-item">
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
          <h1>Lotes</h1>
          <span>{currentUser.email}</span>
        </header>

        <form className="animals-search" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Buscar por código, cor ou criado por"
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
              ? `${filteredLotes.length} ${filteredLotes.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
              : `${filteredLotes.length} ${filteredLotes.length === 1 ? 'lote cadastrado' : 'lotes cadastrados'}`}
        </p>

        {feedback.message ? (
          <p
            className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
          >
            {feedback.message}
          </p>
        ) : null}

        {filteredLotes.length ? (
          <div className="animals-grid">
            {filteredLotes.map((lote) => (
              <LoteCard
                key={lote.id}
                lote={lote}
                onDetalhes={openDetailsModal}
                onEditar={openEditModal}
              />
            ))}
          </div>
        ) : (
          <div className="animals-empty">
            {activeSearch ? (
              <>
                <p>Nenhum lote encontrado.</p>
                <span>
                  Nenhum resultado para {`"${activeSearch}"`}. Ajuste o termo da
                  busca.
                </span>
              </>
            ) : (
              <>
                <p>Nenhum lote cadastrado.</p>
                <span>Clique no botão + para cadastrar o primeiro lote.</span>
              </>
            )}
          </div>
        )}

        {canEdit ? (
          <button
            type="button"
            className="fab-add"
            aria-label="Adicionar lote"
            onClick={openCreateModal}
          >
            +
          </button>
        ) : null}
      </section>

      {modal.type === 'form' ? (
        <LoteFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          feedback={formFeedback}
          setoresDisponiveis={setores}
          animaisDisponiveis={animaisDisponiveis}
          currentUser={currentUser}
          onClose={closeModal}
          onChange={handleFormChange}
          onChangeAlocacoes={handleAlocacoesChange}
          onSubmit={handleSubmitForm}
        />
      ) : null}

      {modal.type === 'details' && modal.lote ? (
        <LoteDetailsModal
          lote={modal.lote}
          onClose={closeModal}
          onEdit={() => openEditModal(modal.lote)}
          onDelete={handleDelete}
          isDeleting={isDeleting}
        />
      ) : null}
    </main>
  )
}

export default LotesPage
