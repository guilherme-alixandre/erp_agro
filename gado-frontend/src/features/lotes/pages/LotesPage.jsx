import { useState, useMemo } from 'react'
import LoteCard from '../components/LoteCard'
import LoteFormModal from '../components/LoteFormModal'
import LoteDetailsModal from '../components/LoteDetailsModal'
import {
  atualizarLote,
  buscarLotePorId,
  buscarLotes,
  cadastrarLote,
  deletarLote,
  getLoteDoPayload,
  exportarLotesCsv,
} from '../../../services/loteApi'
import '../styles/lotes.css'

const defaultForm = {
  descricao: '',
  racaPredominante: '',
  status: 'ATIVO',
}

function LotesPage({ currentUser }) {
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
    setFormData(lote)
    setFormFeedback('')
    setModal({ type: 'form', lote })
  }

  function openDetailsModal(lote) {
    setModal({ type: 'details', lote })
  }

  async function handleSearch(event) {
    event.preventDefault()
    if (!search.trim()) {
      setFeedback({ type: '', message: '' })
      setLotes([])
      return
    }

    setIsLoadingSearch(true)
    setFeedback({ type: '', message: '' })

    try {
      let fetchedLotes = []
      if (!isNaN(Number(search.trim()))) {
        // Se for um número, tenta buscar por ID
        try {
          const lote = await buscarLotePorId(Number(search.trim()))
          fetchedLotes = [lote]
        } catch (error) {
          // Se não encontrar por ID, tenta por descrição
          fetchedLotes = await buscarLotes(null, search.trim())
        }
      } else {
        // Se não for um número, busca por descrição
        fetchedLotes = await buscarLotes(null, search.trim())
      }

      setLotes(fetchedLotes)
      if (fetchedLotes.length > 0) {
        setFeedback({
          type: 'info',
          message: `${fetchedLotes.length} lote(s) encontrado(s).`,
        })
      } else {
        setFeedback({
          type: 'info',
          message: 'Nenhum lote encontrado.',
        })
      }
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Lote não encontrado.',
      })
      setLotes([])
    } finally {
      setIsLoadingSearch(false)
    }
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormFeedback('')

    try {
      const payload = formMode === 'create'
          ? await cadastrarLote({
            ...formData,
            usuario_id: currentUser.id,
          })
          : await atualizarLote(formData.id, formData)

      let lote = getLoteDoPayload(payload)
      if (lote && lote.id && !lote.descricao) {
        lote = await buscarLotePorId(lote.id)
      }
      const isError = !lote || typeof lote !== 'object'

      if (isError) {
        throw new Error(payload?.Erro ?? payload?.erro ?? 'Erro ao salvar lote.')
      }

      if (formMode === 'create') {
        setLotes((current) => [lote, ...current])
        setFeedback({
          type: 'info',
          message: `Lote ${lote.descricao} cadastrado com sucesso!`,
        })
      } else {
        setLotes((current) =>
            current.map((item) => (item.id === lote.id ? lote : item))
        )
        setFeedback({
          type: 'info',
          message: 'Lote atualizado com sucesso!',
        })
      }

      closeModal()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar lote.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(lote) {
    if (!window.confirm(`Deseja excluir o lote "${lote.descricao}"?`)) {
      return
    }

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })

    try {
      await deletarLote(lote.id)
      setLotes((current) => current.filter((item) => item.id !== lote.id))
      setFeedback({
        type: 'info',
        message: `Lote ${lote.descricao} excluído com sucesso!`,
      })
      closeModal()
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao excluir lote.',
      })
    } finally {
      setIsDeleting(false)
    }
  }

  async function handleExportCsv() {
    try {
      const blob = await exportarLotesCsv();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'lotes.csv';
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);
      setFeedback({
        type: 'info',
        message: 'Lotes exportados para CSV com sucesso!',
      });
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao exportar lotes para CSV.',
      });
    }
  }

  return (
      <section className="lotes-content">    <header className="lotes-header">        <h1>Lotes</h1>
        <span>{currentUser.email}</span>
      </header>

        <form className="lotes-search" onSubmit={handleSearch}>
          <input
              type="text"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Pesquisar lote por ID ou descrição"
          />
          <button type="submit" disabled={isLoadingSearch}>
            {isLoadingSearch ? 'Buscando...' : 'Buscar'}
          </button>
          <button type="button" onClick={handleExportCsv} title="Exportar para CSV">
            Exportar CSV
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
            <div className="lotes-grid">          {cards.map((lote) => (
                <LoteCard
                    key={lote.id}
                    lote={lote}
                    onDetalhes={openDetailsModal}
                    onEditar={openEditModal}
                />
            ))}
            </div>
        ) : (
            <div className="lotes-empty">          <p>Nenhum lote carregado.</p>
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
                userEmail={currentUser.email}
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