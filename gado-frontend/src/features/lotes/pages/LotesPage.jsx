import { useMemo, useState } from 'react'
import LoteCard from '../components/LoteCard'
import LoteFormModal from '../components/LoteFormModal'
import LoteDetailsModal from '../components/LoteDetailsModal'
import {
  cadastrarLote,
  buscarLotePorId,
  atualizarLote,
  deletarLote,
} from '../../../services/loteApi'
import '../styles/lotes.css'

const defaultForm = {
  usuario_id: '',
  descricao: '',
  racaPredominante: '',
}

function LotesPage() {
  const [search, setSearch] = useState('')
  const [isLoadingSearch, setIsLoadingSearch] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [lotes, setLotes] = useState([])
  const [modal, setModal] = useState({ type: null, lote: null })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')

  const cards = useMemo(() => lotes, [lotes])

  function closeModal() {
    setModal({ type: null, lote: null })
    setFormData(defaultForm)
    setFormFeedback('')
  }

  function handleFormChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({
      ...current,
      [name]: value,
    }))
  }

  function openCreateModal() {
    setFormMode('create')
    setFormData(defaultForm)
    setFormFeedback('')
    setModal({ type: 'form', lote: null })
  }

  function openEditModal(lote) {
    setFormMode('edit')
    setFormFeedback('')
    setFormData({
      ...defaultForm,
      ...lote,
      usuario_id: String(lote.usuario_id ?? ''),
    })
    setModal({ type: 'form', lote })
  }

  function openDetailsModal(lote) {
    setModal({ type: 'details', lote })
  }

  async function handleSearch(event) {
    event.preventDefault()
    const id = search.trim()
    if (!id) return

    setIsLoadingSearch(true)
    setFeedback({ type: '', message: '' })
    try {
      const lote = await buscarLotePorId(id)
      const index = lotes.findIndex((item) => item.id === lote.id)
      if (index === -1) {
        setLotes((current) => [lote, ...current])
      } else {
        const updatedLotes = [...lotes]
        updatedLotes[index] = lote
        setLotes(updatedLotes)
      }
      setSearch('')
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao buscar lote.',
      })
    } finally {
      setIsLoadingSearch(false)
    }
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    try {
      if (formMode === 'create') {
        const result = await cadastrarLote(formData)
        const loadedLote = await buscarLotePorId(result.id || formData.id)
        setLotes((current) => [loadedLote, ...current])
        setFeedback({ type: 'info', message: 'Lote cadastrado com sucesso.' })
      } else {
        const result = await atualizarLote(formData.id, formData)
        setLotes((current) =>
          current.map((lote) =>
            lote.id === formData.id
              ? {
                  ...lote,
                  ...formData,
                  usuario_id: Number(formData.usuario_id),
                }
              : lote,
          ),
        )
        setFeedback({ type: 'info', message: 'Lote atualizado com sucesso.' })
      }

      closeModal()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar o lote.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(lote) {
    const confirmDelete = window.confirm(
      `Deseja excluir o lote ${lote.descricao}?`,
    )
    if (!confirmDelete) return

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })
    try {
      await deletarLote(lote.id)
      setLotes((current) =>
        current.filter((item) => item.id !== lote.id),
      )
      closeModal()
      setFeedback({ type: 'info', message: 'Lote excluído com sucesso.' })
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
    <section className="animals-content">
      <header className="animals-header">
        <h1>Lotes</h1>
        <span>Perfil</span>
      </header>

      <form className="animals-search" onSubmit={handleSearch}>
        <input
          type="text"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Pesquisar lote por ID"
        />
        <button type="submit" disabled={isLoadingSearch}>
          {isLoadingSearch ? 'Buscando...' : 'Buscar'}
        </button>
      </form>

      {feedback.message ? (
        <p
          className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
        >
          {feedback.message}
        </p>
      ) : null}

      {cards.length ? (
        <div className="animals-grid">
          {cards.map((lote) => (
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
          <p>Nenhum lote carregado.</p>
          <span>
            Busque por ID para carregar um lote ou clique no botão +
            para cadastrar.
          </span>
        </div>
      )}

      <button
        type="button"
        className="fab-add"
        aria-label="Adicionar lote"
        onClick={openCreateModal}
      >
        +
      </button>

      {modal.type === 'form' ? (
        <LoteFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          feedback={formFeedback}
          onClose={closeModal}
          onChange={handleFormChange}
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
    </section>
  )
}

export default LotesPage
