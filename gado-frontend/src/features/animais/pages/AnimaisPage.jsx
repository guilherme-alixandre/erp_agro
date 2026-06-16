import { useCallback, useEffect, useMemo, useState } from 'react'
import AnimalFormModal from '../components/AnimalFormModal'
import AnimalDetailsModal from '../components/AnimalDetailsModal'
import {
  atualizarAnimal,
  buscarAnimais,
  cadastrarAnimal,
  deletarAnimal,
  getBackendMessage,
  isBackendErrorMessage,
} from '../../../services/animalApi'
import { listarVacinas } from '../../../services/insumoApi'
import { atualizarLote, listarLotesCompletos } from '../../../services/loteApi'
import { useRefresh } from '../../../contexts/RefreshContext.jsx'
import '../styles/animais.css'

const defaultForm = {
  codigoBrinco: '',
  nome: '',
  dataNascimento: '',
  pesoAtual: '',
  raca: '',
  cor: '',
  alturaCernelha: '',
  perimetroToracico: '',
  comprimentoCorporal: '',
  sexo: 'M',
  statusAnimal: 'ATIVO',
  vacinas: [],
}

function calcAgeLabel(dateText) {
  if (!dateText) return 'idade não informada'
  const [birthYear, birthMonth, birthDay] = dateText
    .split('-')
    .map((part) => Number(part))
  if (!birthYear || !birthMonth || !birthDay) return 'idade não informada'

  const now = new Date()
  let years = now.getFullYear() - birthYear
  const monthDiff = now.getMonth() + 1 - birthMonth
  const dayDiff = now.getDate() - birthDay
  if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
    years -= 1
  }
  if (years < 1) return 'menos de 1 ano'
  return `${years} ${years > 1 ? 'anos' : 'ano'}`
}

function toCardAnimal(animal) {
  return {
    ...animal,
    idadeLabel: calcAgeLabel(animal.dataNascimento),
    pesoLabel: `${Number(animal.pesoAtual || 0).toFixed(0)} KG`,
  }
}

const ROWS_PER_PAGE = 10

function AnimaisPage({ currentUser, onNavigate, onLogout }) {
  const [search, setSearch] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [animals, setAnimals] = useState([])
  const [modal, setModal] = useState({ type: null, animal: null })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')
  const [vacinasDisponiveis, setVacinasDisponiveis] = useState([])
  const [lotesDisponiveis, setLotesDisponiveis] = useState([])
  const [loteVinculo, setLoteVinculo] = useState(null)
  const [setorVinculo, setSetorVinculo] = useState(null)
  const [filterSexo, setFilterSexo] = useState('')
  const [filterStatus, setFilterStatus] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')
  const [page, setPage] = useState(0)

  const { refreshGlobal, dispararRefresh } = useRefresh()

  const cards = useMemo(() => animals.map(toCardAnimal), [animals])

  const filteredCards = useMemo(() => {
    return cards.filter((a) => {
      if (filterSexo && a.sexo !== filterSexo) return false
      if (filterStatus && a.statusAnimal !== filterStatus) return false
      if (dateFrom && a.dataNascimento && a.dataNascimento < dateFrom) return false
      if (dateTo && a.dataNascimento && a.dataNascimento > dateTo) return false
      return true
    })
  }, [cards, filterSexo, filterStatus, dateFrom, dateTo])

  const totalPages = Math.max(1, Math.ceil(filteredCards.length / ROWS_PER_PAGE))
  const paginatedCards = filteredCards.slice(
    page * ROWS_PER_PAGE,
    (page + 1) * ROWS_PER_PAGE,
  )

  const fetchAnimals = useCallback(async (termo) => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await buscarAnimais(termo)
      setAnimals(list)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar animais.',
      })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchAnimals('')
  }, [fetchAnimals, refreshGlobal])

  function handleSearchSubmit(event) {
    event.preventDefault()
    const termo = search.trim()
    setActiveSearch(termo)
    setPage(0)
    fetchAnimals(termo)
  }

  function handleClearSearch() {
    setSearch('')
    setActiveSearch('')
    setPage(0)
    fetchAnimals('')
  }

  function closeModal() {
    setModal({ type: null, animal: null })
    setFormData(defaultForm)
    setFormFeedback('')
    setLoteVinculo(null)
    setSetorVinculo(null)
  }

  function handleFormChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({
      ...current,
      [name]: value,
    }))
  }

  function handleAddVacina() {
    setFormData((current) => ({
      ...current,
      vacinas: [...(current.vacinas ?? []), { nome: '', dataOcorrencia: '' }],
    }))
  }

  function handleChangeVacina(index, field, value) {
    setFormData((current) => {
      const next = [...(current.vacinas ?? [])]
      next[index] = { ...next[index], [field]: value }
      return { ...current, vacinas: next }
    })
  }

  function handleRemoveVacina(index) {
    setFormData((current) => ({
      ...current,
      vacinas: (current.vacinas ?? []).filter((_, i) => i !== index),
    }))
  }

  async function carregarVacinasDisponiveis() {
    try {
      const lista = await listarVacinas('')
      setVacinasDisponiveis(lista)
    } catch {
      setVacinasDisponiveis([])
    }
  }

  async function carregarLotesDisponiveis() {
    try {
      const lista = await listarLotesCompletos()
      setLotesDisponiveis(lista.filter((l) => l.statusLote === 'ATIVO'))
    } catch {
      setLotesDisponiveis([])
    }
  }

  function handleChangeLoteVinculo(e) {
    const val = e.target.value
    setLoteVinculo(val ? Number(val) : null)
    setSetorVinculo(null)
  }

  function handleChangeSetorVinculo(e) {
    const val = e.target.value
    setSetorVinculo(val ? Number(val) : null)
  }

  function openCreateModal() {
    setFormMode('create')
    setFormData(defaultForm)
    setFormFeedback('')
    setLoteVinculo(null)
    setSetorVinculo(null)
    setModal({ type: 'form', animal: null })
    carregarVacinasDisponiveis()
    carregarLotesDisponiveis()
  }

  function openEditModal(animal) {
    setFormMode('edit')
    setFormFeedback('')
    setFormData({
      ...defaultForm,
      ...animal,
      pesoAtual: String(animal.pesoAtual ?? ''),
      vacinas: [],
    })
    setModal({ type: 'form', animal })
  }

  function openDetailsModal(animal) {
    setModal({ type: 'details', animal })
  }

  async function handleSubmitForm(event) {
    event.preventDefault()

    if (formMode === 'create' && loteVinculo && !setorVinculo) {
      setFormFeedback('Selecione um setor para vincular ao lote.')
      return
    }

    setIsSaving(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    try {
      if (formMode === 'create') {
        const result = await cadastrarAnimal(currentUser.email, formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(getBackendMessage(result) || 'Falha ao cadastrar animal.')
        }

        if (loteVinculo && setorVinculo) {
          try {
            const novoAnimalId = result?.id ?? result?.mensagem?.id ?? null
            if (novoAnimalId !== null) {
              const lote = lotesDisponiveis.find((l) => l.id === loteVinculo)
              if (lote) {
                const alocacoesUpdate = lote.alocacoes.map((aloc) => ({
                  setorId: aloc.setorId,
                  animaisIds: [
                    ...aloc.animais.map((a) => a.id).filter((id) => id !== null),
                    ...(Number(aloc.loteSectorId) === setorVinculo ? [novoAnimalId] : []),
                  ],
                }))
                await atualizarLote(lote.id, currentUser.email, {
                  corBrinco: lote.corBrinco,
                  descricao: lote.descricao,
                  racaPredominante: lote.racaPredominante,
                  alocacoes: alocacoesUpdate,
                })
                setFeedback({ type: 'info', message: 'Animal cadastrado e vinculado ao lote com sucesso.' })
              } else {
                setFeedback({ type: 'info', message: 'Animal cadastrado com sucesso.' })
              }
            } else {
              setFeedback({ type: 'info', message: 'Animal cadastrado. Vincule-o ao lote manualmente na página de Lotes.' })
            }
          } catch (loteError) {
            setFeedback({ type: 'info', message: `Animal cadastrado, mas falha ao vincular ao lote: ${loteError.message}` })
          }
        } else {
          setFeedback({ type: 'info', message: 'Animal cadastrado com sucesso.' })
        }
      } else {
        const result = await atualizarAnimal(formData.codigoBrinco, formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(getBackendMessage(result) || 'Falha ao atualizar animal.')
        }
        setFeedback({ type: 'info', message: 'Animal atualizado com sucesso.' })
      }

      dispararRefresh()
      closeModal()
      await fetchAnimals(activeSearch)
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar o animal.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(animal) {
    const confirmDelete = window.confirm(
      `Deseja excluir o animal ${animal.codigoBrinco}?`,
    )
    if (!confirmDelete) return

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })
    try {
      const result = await deletarAnimal(animal.codigoBrinco)
      if (isBackendErrorMessage(result)) {
        throw new Error(getBackendMessage(result) || 'Falha ao excluir animal.')
      }

      closeModal()
      setFeedback({ type: 'info', message: 'Animal excluído com sucesso.' })
      await fetchAnimals(activeSearch)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao excluir animal.',
      })
    } finally {
      setIsDeleting(false)
    }
  }

  function exportCSV() {
    const headers = ['Código Brinco', 'Nome', 'Raça', 'Sexo', 'Peso (KG)', 'Nascimento', 'Status']
    const rows = filteredCards.map((a) => [
      a.codigoBrinco,
      a.nome || '',
      a.raca || '',
      a.sexo === 'M' ? 'Macho' : 'Fêmea',
      Number(a.pesoAtual || 0).toFixed(0),
      a.dataNascimento || '',
      a.statusAnimal,
    ])
    const csvContent = [headers, ...rows]
      .map((row) => row.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(','))
      .join('\n')
    const blob = new Blob(['﻿' + csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'animais.csv'
    link.click()
    URL.revokeObjectURL(url)
  }

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo">🌿</div>
        <nav>
          <button type="button" className="menu-item menu-item--active">
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
          <h1>Animais</h1>
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
              placeholder="Buscar por nome ou código do brinco"
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

          <select
            className="toolbar-select"
            value={filterSexo}
            onChange={(e) => {
              setFilterSexo(e.target.value)
              setPage(0)
            }}
          >
            <option value="">Todos os Sexos</option>
            <option value="M">Macho</option>
            <option value="F">Fêmea</option>
          </select>

          <select
            className="toolbar-select"
            value={filterStatus}
            onChange={(e) => {
              setFilterStatus(e.target.value)
              setPage(0)
            }}
          >
            <option value="">Todos os Status</option>
            <option value="ATIVO">Ativo</option>
            <option value="OBSERVACAO">Observação</option>
            <option value="VENDIDO">Vendido</option>
            <option value="OBITO">Obito</option>
            <option value="ABATIDO">Abatido</option>
          </select>

          <input
            type="date"
            className="toolbar-date"
            value={dateFrom}
            title="Nascimento de"
            onChange={(e) => {
              setDateFrom(e.target.value)
              setPage(0)
            }}
          />

          <input
            type="date"
            className="toolbar-date"
            value={dateTo}
            title="Nascimento até"
            onChange={(e) => {
              setDateTo(e.target.value)
              setPage(0)
            }}
          />

          <button type="button" className="btn-export-csv" onClick={exportCSV}>
            Exportar CSV
          </button>

          <button type="button" className="btn-new-entity" onClick={openCreateModal}>
            + Novo Animal
          </button>
        </div>

        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Código Brinco</th>
                <th>Nome</th>
                <th>Raça</th>
                <th>Sexo</th>
                <th>Peso</th>
                <th>Idade</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="table-loading">
                    Carregando...
                  </td>
                </tr>
              ) : paginatedCards.length === 0 ? (
                <tr>
                  <td colSpan={8} className="table-empty">
                    {activeSearch
                      ? `Nenhum resultado para "${activeSearch}".`
                      : 'Nenhum animal cadastrado. Clique em "+ Novo Animal" para começar.'}
                  </td>
                </tr>
              ) : (
                paginatedCards.map((animal) => (
                  <tr key={animal.codigoBrinco}>
                    <td className="td-mono">{animal.codigoBrinco}</td>
                    <td>{animal.nome || '—'}</td>
                    <td>{animal.raca || '—'}</td>
                    <td>{animal.sexo === 'M' ? 'Macho' : 'Fêmea'}</td>
                    <td>{animal.pesoLabel}</td>
                    <td>{animal.idadeLabel}</td>
                    <td>
                      <span
                        className={`status-pill ${
                          animal.statusAnimal === 'ATIVO'
                            ? 'status-pill--ativo'
                            : 'status-pill--inativo'
                        }`}
                      >
                        {animal.statusAnimal}
                      </span>
                    </td>
                    <td>
                      <div className="row-actions">
                        <button
                          type="button"
                          className="btn-row"
                          onClick={() => openDetailsModal(animal)}
                        >
                          Detalhes
                        </button>
                        <button
                          type="button"
                          className="btn-row btn-row--edit"
                          onClick={() => openEditModal(animal)}
                        >
                          Editar
                        </button>
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
              : `${filteredCards.length} ${filteredCards.length === 1 ? 'registro' : 'registros'}`}
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
        <AnimalFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          feedback={formFeedback}
          userEmail={currentUser.email}
          vacinasDisponiveis={vacinasDisponiveis}
          lotesDisponiveis={lotesDisponiveis}
          loteVinculo={loteVinculo}
          setorVinculo={setorVinculo}
          onClose={closeModal}
          onChange={handleFormChange}
          onSubmit={handleSubmitForm}
          onAddVacina={handleAddVacina}
          onChangeVacina={handleChangeVacina}
          onRemoveVacina={handleRemoveVacina}
          onChangeLote={handleChangeLoteVinculo}
          onChangeSetor={handleChangeSetorVinculo}
        />
      ) : null}

      {modal.type === 'details' && modal.animal ? (
        <AnimalDetailsModal
          animal={toCardAnimal(modal.animal)}
          onClose={closeModal}
          onEdit={() => openEditModal(modal.animal)}
          onDelete={handleDelete}
          isDeleting={isDeleting}
        />
      ) : null}
    </main>
  )
}

export default AnimaisPage
