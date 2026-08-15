import { useCallback, useEffect, useState } from 'react'
import LoteCard from '../components/LoteCard'
import LoteFormModal from '../components/LoteFormModal'
import LoteDetailsModal from '../components/LoteDetailsModal'
import {
  listarLotes,
  listarAnimaisParaLote,
  cadastrarLote,
  atualizarLote,
  deletarLote,
  transferirAnimal,
  exportarLotesCSV,
  exportarLotesPDF,
} from '../integration/loteApi'
import { listarSetores } from '../../setores/integration/setorApi'
import '../../animais/styles/animais.css'
import '../styles/lotes.css'

const PERFIS_COM_CRIACAO_LOTE = ['ADMINISTRADOR', 'GERENTE']
const PERFIS_COM_EDICAO_LOTE = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR_CHEFE']
const PERFIS_COM_TRANSFERENCIA = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR_CHEFE']

const defaultForm = {
  codigo: '',
  corBrinco: '',
  descricao: '',
  racaPredominante: '',
  dataCriacao: '',
  alocacoes: [],
}

function LotesPage({ currentUser, onNavigate, onLogout }) {
  const [search, setSearch] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [lotes, setLotes] = useState([])
  const [setores, setSetores] = useState([])
  const [modal, setModal] = useState({ type: null, lote: null })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')
  const [animaisDisponiveis, setAnimaisDisponiveis] = useState([])

  const canCreateLote = PERFIS_COM_CRIACAO_LOTE.includes(currentUser?.perfil)
  const canEditLote = PERFIS_COM_EDICAO_LOTE.includes(currentUser?.perfil)
  const canDeleteLote = PERFIS_COM_CRIACAO_LOTE.includes(currentUser?.perfil)
  const canTransfer = PERFIS_COM_TRANSFERENCIA.includes(currentUser?.perfil)

  const fetchLotes = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await listarLotes()
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
    listarSetores()
      .then(setSetores)
      .catch(() => setSetores([]))
  }, [fetchLotes])

  async function carregarAnimaisParaLote(loteAtualId) {
    try {
      const lista = await listarAnimaisParaLote()
      const ocupados = new Set(
        lotes
          .filter((l) => l.id !== loteAtualId)
          .flatMap((l) => l.alocacoes.flatMap((aloc) => aloc.animais.map((a) => a.id)))
          .filter((id) => id !== null),
      )
      setAnimaisDisponiveis(lista.filter((a) => !ocupados.has(a.id)))
    } catch {
      setAnimaisDisponiveis([])
    }
  }

  const lotesFiltrados = activeSearch
    ? lotes.filter((l) => {
        const termo = activeSearch.toLowerCase()
        return (
          l.codigo.toLowerCase().includes(termo) ||
          l.corBrinco.toLowerCase().includes(termo) ||
          (l.criadoPorNome ?? '').toLowerCase().includes(termo)
        )
      })
    : lotes

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
    carregarAnimaisParaLote(null)
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
        animaisAtuais: aloc.animais,
      })),
    })
    setModal({ type: 'form', lote })
    carregarAnimaisParaLote(lote.id)
  }

  function openDetailsModal(lote) {
    setModal({ type: 'details', lote })
  }

  async function handleTransferirAnimais(animalIds, loteDestinoId, setorDestinoId) {
    let transferError = null
    try {
      await Promise.all(
        animalIds.map((animalId) =>
          transferirAnimal(currentUser.email, animalId, loteDestinoId, setorDestinoId),
        ),
      )
    } catch (e) {
      transferError = e
    }

    const updatedLotes = await listarLotes()
    setLotes(updatedLotes)
    const loteAtualizado = updatedLotes.find((l) => l.id === modal.lote?.id)
    if (loteAtualizado) {
      setFormData((current) => ({
        ...current,
        alocacoes: loteAtualizado.alocacoes.map((aloc) => ({
          setorId: aloc.setorId,
          animaisIds: aloc.animais.map((a) => a.id),
          animaisAtuais: aloc.animais,
        })),
      }))
      setModal((current) => ({ ...current, lote: loteAtualizado }))
    }

    if (transferError) throw transferError
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

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
    if (!window.confirm(`Deseja excluir o lote ${lote.codigo}?`)) return

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
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button type="button" className="menu-item menu-item--active">
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
          <h1>Lotes</h1>
          <span>{currentUser.email}</span>
        </header>

        <div className="lotes-export">
          <button type="button" onClick={() => exportarLotesCSV(lotesFiltrados)}>
            Exportar CSV
          </button>
          <button type="button" onClick={exportarLotesPDF}>
            Exportar PDF
          </button>
        </div>

        <form className="animals-search" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar por código, cor ou criado por"
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
              ? `${lotesFiltrados.length} ${lotesFiltrados.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
              : `${lotes.length} ${lotes.length === 1 ? 'lote cadastrado' : 'lotes cadastrados'}`}
        </p>

        {feedback.message && (
          <p className={`feedback feedback--${feedback.type === 'error' ? 'error' : 'info'}`}>
            {feedback.message}
          </p>
        )}

        {lotesFiltrados.length ? (
          <div className="animals-grid">
            {lotesFiltrados.map((lote) => (
              <LoteCard key={lote.id} lote={lote} onDetalhes={openDetailsModal} onEditar={openEditModal} />
            ))}
          </div>
        ) : (
          <div className="animals-empty">
            {activeSearch ? (
              <>
                <p>Nenhum lote encontrado.</p>
                <span>Nenhum resultado para "{activeSearch}". Ajuste o termo da busca.</span>
              </>
            ) : (
              <>
                <p>Nenhum lote cadastrado.</p>
                <span>Clique no botão + para cadastrar o primeiro lote.</span>
              </>
            )}
          </div>
        )}

        {canCreateLote && (
          <button type="button" className="fab-add" aria-label="Adicionar lote" onClick={openCreateModal}>
            +
          </button>
        )}
      </section>

      {modal.type === 'form' && (
        <LoteFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          feedback={formFeedback}
          setoresDisponiveis={setores}
          animaisDisponiveis={animaisDisponiveis}
          lotesDisponiveis={lotes}
          loteAtualId={modal.lote?.id ?? null}
          canTransfer={canTransfer}
          currentUser={currentUser}
          onClose={closeModal}
          onChange={handleFormChange}
          onChangeAlocacoes={handleAlocacoesChange}
          onTransferirAnimais={handleTransferirAnimais}
          onSubmit={handleSubmitForm}
        />
      )}

      {modal.type === 'details' && modal.lote && (
        <LoteDetailsModal
          lote={modal.lote}
          onClose={closeModal}
          onEdit={() => openEditModal(modal.lote)}
          onDelete={handleDelete}
          isDeleting={isDeleting}
          canEdit={canEditLote}
          canDelete={canDeleteLote}
        />
      )}
    </main>
  )
}

export default LotesPage
