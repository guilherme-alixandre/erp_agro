import { useState, useMemo, useEffect } from 'react'
import SetorCard from '../components/SetorCard'
import SetorFormModal from '../components/SetorFormModal'
import SetorDetailsModal from '../components/SetorDetailsModal'
import {
  atualizarSetor,
  buscarSetorPorId,
  buscarSetores,
  cadastrarSetor,
  deletarSetor,
  exportarSetoresCsv,
  getSetorDoPayload,
} from '../../../services/setorApi'
import '../styles/setores.css'

const defaultForm = {
  descricao: '',
  usuario_id: null,
}

function SetoresPage({ currentUser }) {
  const [search, setSearch] = useState('')
  const [isLoadingSearch, setIsLoadingSearch] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [setores, setSetores] = useState([])
  const [currentPage, setCurrentPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [pageSize, setPageSize] = useState(16)
  const [modal, setModal] = useState({ type: null, setor: null })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')

  const cards = useMemo(() => setores, [setores])

  async function fetchSetores(page = 0, size = pageSize) {
    setIsLoadingSearch(true);
    setFeedback({ type:
          '\'', message: '' });
    try {
      const isIdSearch = !isNaN(search.trim()) && search.trim() !== '';
      const id = isIdSearch ? Number(search.trim()) : null;
      const descricao = isIdSearch ? null : search.trim();

      const response = await buscarSetores(id, descricao, page, size);
      setSetores(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(response.number);
      setFeedback({
        type: 'info',
        message: `${response.totalElements} setor(es) encontrado(s).`,
      });
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Erro ao buscar setores.',
      });
      setSetores([]);
      setTotalPages(0);
      setCurrentPage(0);
    } finally {
      setIsLoadingSearch(false);
    }
  }

  useEffect(() => {
    fetchSetores();
  }, [pageSize]);

  function closeModal() {
    setModal({ type: null, setor: null })
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
    setModal({ type: 'form', setor: null })
  }

  function openEditModal(setor) {
    setFormMode('edit')
    setFormData(setor)
    setFormFeedback('')
    setModal({ type: 'form', setor })
  }

  function openDetailsModal(setor) {
    setModal({ type: 'details', setor })
  }

  async function handleSearch(event) {
    event.preventDefault();
    fetchSetores();
  }

  async function handleExportCsv() {
    try {
      await exportarSetoresCsv();
      setFeedback({
        type: 'info',
        message: 'Exportação CSV iniciada com sucesso!',
      });
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao exportar setores para CSV.',
      });
    }
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormFeedback('')

    try {
      const payload = formMode === 'create'
          ? await cadastrarSetor(formData)
          : await atualizarSetor(formData.id, formData)

      const setor = getSetorDoPayload(payload)
      const isError = !setor || typeof setor !== 'object'

      if (isError) {
        throw new Error(payload?.Erro ?? payload?.erro ?? 'Erro ao salvar setor.')
      }

      if (formMode === 'create') {
        setSetores((current) => [setor, ...current])
        setFeedback({
          type: 'info',
          message: `Setor ${setor.descricao} cadastrado com sucesso!`,
        })
      } else {
        setSetores((current) =>
            current.map((item) => (item.id === setor.id ? setor : item))
        )
        setFeedback({
          type: 'info',
          message: `Setor ${setor.descricao} atualizado com sucesso!`,
        })
      }

      closeModal()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar setor.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(setor) {
    if (!window.confirm(`Deseja excluir o setor "${setor.nome}"?`)) {
      return
    }

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })

    try {
      await deletarSetor(setor.id)
      setSetores((current) => current.filter((item) => item.id !== setor.id))
      setFeedback({
        type: 'info',
        message: `Setor ${setor.descricao} excluído com sucesso!`,
      })
      closeModal()
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
      <section className="setores-content">
        <header className="setores-header">
          <h1>Setores</h1>
          <span>{currentUser.email}</span>
        </header>

        <form className="setores-search" onSubmit={handleSearch}>
          <input
              type="text"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Pesquisar setor por ID ou nome"
          />
          <button type="submit" disabled={isLoadingSearch}>
            {isLoadingSearch ? 'Buscando...' : 'Buscar'}
          </button>
          <button type="button" onClick={handleExportCsv} className="export-csv-button">
            Exportar CSV
          </button>
        </form>

        <div className="pagination-controls">
          <label htmlFor="pageSize">Itens por página:</label>
          <select
              id="pageSize"
              value={pageSize}
              onChange={(e) => setPageSize(Number(e.target.value))}
          >
            <option value={16}>16</option>
            <option value={32}>32</option>
          </select>
          <button
              onClick={() => fetchSetores(currentPage - 1)}
              disabled={currentPage === 0 || isLoadingSearch}
          >
            Anterior
          </button>
          <span>Página {currentPage + 1} de {totalPages}</span>
          <button
              onClick={() => fetchSetores(currentPage + 1)}
              disabled={currentPage === totalPages - 1 || isLoadingSearch}
          >
            Próxima
          </button>
        </div>

        {feedback.message ? (
            <p
                className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
            >
              {feedback.message}
            </p>
        ) : null}

        {cards.length ? (
            <div className="setores-grid">
              {cards.map((setor) => (
                  <SetorCard
                      key={setor.id}
                      setor={setor}
                      onDetalhes={openDetailsModal}
                      onEditar={openEditModal}
                  />
              ))}
            </div>
        ) : (
            <div className="setores-empty">
              <p>Nenhum setor carregado.</p>
              <span>
            Busque por ID para carregar um setor ou clique no botão +
            para cadastrar.
          </span>
            </div>
        )}

        <button
            type="button"
            className="fab-add"
            aria-label="Adicionar setor"
            onClick={openCreateModal}
        >
          +
        </button>

        {modal.type === 'form' ? (
            <SetorFormModal
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

        {modal.type === 'details' && modal.setor ? (
            <SetorDetailsModal
                setor={modal.setor}
                onClose={closeModal}
                onEdit={() => openEditModal(modal.setor)}
                onDelete={handleDelete}
                isDeleting={isDeleting}
            />
        ) : null}
      </section>
  )
}

export default SetoresPage
