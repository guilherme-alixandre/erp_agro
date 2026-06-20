import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import LoteFormModal from '../components/LoteFormModal'
import LoteDetailsModal from '../components/LoteDetailsModal'
import {
  listarLotesCompletos,
  listarAnimaisParaLote,
  cadastrarLote,
  atualizarLote,
  deletarLote,
  transferirAnimal,
  exportarLotesCSV,
  exportarLotesPDF,
} from '../../../services/loteApi'
import { useRefresh } from '../../../contexts/RefreshContext.jsx'
import '../../animais/styles/animais.css'
import '../styles/lotes.css'

const PERFIS_COM_EDICAO = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR']
const PERFIS_COM_TRANSFERENCIA = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR_CHEFE']

const defaultForm = {
  codigo: '',
  corBrinco: '',
  descricao: '',
  racaPredominante: '',
  dataCriacao: '',
  alocacoes: [],
}

function formatDate(dateStr) {
  if (!dateStr) return '—'
  const parts = dateStr.split('-')
  if (parts.length !== 3) return dateStr
  return `${parts[2]}/${parts[1]}/${parts[0]}`
}

const ROWS_PER_PAGE = 10

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
  const [exportMenuOpen, setExportMenuOpen] = useState(false)
  const exportMenuRef = useRef(null)
  const [page, setPage] = useState(0)

  const { refreshGlobal, dispararRefresh } = useRefresh()

  const canEdit = PERFIS_COM_EDICAO.includes(currentUser?.perfil)
  const canTransfer = PERFIS_COM_TRANSFERENCIA.includes(currentUser?.perfil)

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
    exportarLotesCSV(filteredLotes)
    setExportMenuOpen(false)
  }

  function handleExportarPDF() {
    exportarLotesPDF()
    setExportMenuOpen(false)
  }

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

  const totalPages = Math.max(1, Math.ceil(filteredLotes.length / ROWS_PER_PAGE))
  const paginatedLotes = filteredLotes.slice(
    page * ROWS_PER_PAGE,
    (page + 1) * ROWS_PER_PAGE,
  )

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
  }, [fetchLotes, refreshGlobal])

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

    // Atualiza sempre para refletir qualquer transferência parcialmente concluída
    const updatedLotes = await listarLotesCompletos()
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
      dispararRefresh()
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
          <h1>Lotes</h1>
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
              placeholder="Buscar por código, cor ou criado por"
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
              + Novo Lote
            </button>
          ) : null}
        </div>

        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Cor Brinco</th>
                <th>Raça Predominante</th>
                <th>Setores</th>
                <th>Total Animais</th>
                <th>Data Criação</th>
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
              ) : paginatedLotes.length === 0 ? (
                <tr>
                  <td colSpan={7} className="table-empty">
                    {activeSearch
                      ? `Nenhum resultado para "${activeSearch}".`
                      : 'Nenhum lote cadastrado. Clique em "+ Novo Lote" para começar.'}
                  </td>
                </tr>
              ) : (
                paginatedLotes.map((lote) => {
                  const totalAnimais = lote.alocacoes.reduce(
                    (sum, aloc) => sum + (aloc.animais?.length || 0),
                    0,
                  )
                  return (
                    <tr key={lote.id}>
                      <td className="td-mono">{lote.codigo}</td>
                      <td>{lote.corBrinco || '—'}</td>
                      <td>{lote.racaPredominante || '—'}</td>
                      <td>{lote.alocacoes.length}</td>
                      <td>{totalAnimais}</td>
                      <td>{formatDate(lote.dataCriacao)}</td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="btn-row"
                            onClick={() => openDetailsModal(lote)}
                          >
                            Detalhes
                          </button>
                          {canEdit ? (
                            <button
                              type="button"
                              className="btn-row btn-row--edit"
                              onClick={() => openEditModal(lote)}
                            >
                              Editar
                            </button>
                          ) : null}
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
              : `${filteredLotes.length} ${filteredLotes.length === 1 ? 'registro' : 'registros'}`}
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
