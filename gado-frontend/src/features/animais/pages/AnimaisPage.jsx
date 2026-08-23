import { useCallback, useEffect, useMemo, useState } from 'react'
import AnimalFormModal from '../components/AnimalFormModal'
import AnimalDetailsModal from '../components/AnimalDetailsModal'
import RacaFormModal from '../components/RacaFormModal'
import {
    buscarAnimais,
    cadastrarAnimal,
    atualizarAnimal,
    deletarAnimal,
    getBackendMessage,
    isBackendErrorMessage,
} from '../integration/animalApi.js'
import {
    listarRacas,
    cadastrarRaca,
    atualizarRaca,
    deletarRaca,
    reativarRaca,
} from '../integration/racaApi.js'
import '../styles/animais.css'
import '../../insumos/styles/insumos.css'

// ============================================================================
// Default Form
// ============================================================================

const DEFAULT_FORM = {
    codigoBrinco: '',
    dataNascimento: '',
    pesoAtual: '',
    racaId: '',
    cor: '',
    alturaCernelha: '',
    perimetroToracico: '',
    comprimentoCorporal: '',
    sexo: 'M',
    statusAnimal: 'ATIVO',
}

const DEFAULT_RACA_FORM = { id: null, nome: '', sigla: '' }

const ROWS_PER_PAGE = 10

// ============================================================================
// Helpers
// ============================================================================

function calcAgeLabel(dateText) {
    if (!dateText) return 'idade não informada'

    const [birthYear, birthMonth, birthDay] = dateText.split('-').map(Number)
    if (!birthYear || !birthMonth || !birthDay) return 'idade não informada'

    const now = new Date()
    let years = now.getFullYear() - birthYear
    const monthDiff = now.getMonth() + 1 - birthMonth
    const dayDiff = now.getDate() - birthDay

    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
        years -= 1
    }

    if (years < 1) {
        let months = (now.getFullYear() - birthYear) * 12 + (now.getMonth() + 1 - birthMonth)
        if (dayDiff < 0) months -= 1
        months = Math.max(0, months)
        if (months < 1) return 'menos de 1 mês'
        return `${months} ${months > 1 ? 'meses' : 'mês'}`
    }
    return `${years} ${years > 1 ? 'anos' : 'ano'}`
}

function toCardAnimal(animal) {
    return {
        ...animal,
        idadeLabel: calcAgeLabel(animal.dataNascimento),
        pesoLabel: `${Number(animal.pesoAtual || 0).toFixed(0)} KG`,
    }
}

function exportAnimaisCSV(animais) {
    const headers = ['Código Brinco', 'Raça', 'Sexo', 'Peso (KG)', 'Nascimento', 'Status']
    const rows = animais.map((a) => [
        a.codigoBrinco,
        a.racaNome || '',
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

// ============================================================================
// Component
// ============================================================================

function AnimaisPage({ currentUser, onNavigate, onLogout }) {
    const [activeTab, setActiveTab] = useState('animais')

    // Estado: Busca
    const [search, setSearch] = useState('')
    const [activeSearch, setActiveSearch] = useState('')

    // Estado: Filtros
    const [filterSexo, setFilterSexo] = useState('')
    const [filterStatus, setFilterStatus] = useState('')
    const [dateFrom, setDateFrom] = useState('')
    const [dateTo, setDateTo] = useState('')
    const [page, setPage] = useState(0)

    // Estado: Carregamento
    const [isLoading, setIsLoading] = useState(false)
    const [isSaving, setIsSaving] = useState(false)
    const [isDeleting, setIsDeleting] = useState(false)

    // Estado: Feedback
    const [feedback, setFeedback] = useState({ type: '', message: '' })
    const [formFeedback, setFormFeedback] = useState('')

    // Estado: Dados
    const [animals, setAnimals] = useState([])
    const [racasDisponiveis, setRacasDisponiveis] = useState([])

    // Estado: Modal
    const [modal, setModal] = useState({ type: null, animal: null })
    const [formMode, setFormMode] = useState('create')
    const [formData, setFormData] = useState(DEFAULT_FORM)

    // Estado: aba Raças
    const [racas, setRacas] = useState([])
    const [isLoadingRacas, setIsLoadingRacas] = useState(false)
    const [racasFeedback, setRacasFeedback] = useState({ type: '', message: '' })
    const [racaSearch, setRacaSearch] = useState('')
    const [racaActiveSearch, setRacaActiveSearch] = useState('')
    const [racaStatusFiltro, setRacaStatusFiltro] = useState('ATIVO')
    const [racaModal, setRacaModal] = useState({ open: false, raca: null })
    const [racaFormMode, setRacaFormMode] = useState('create')
    const [racaFormData, setRacaFormData] = useState(DEFAULT_RACA_FORM)
    const [racaFormFeedback, setRacaFormFeedback] = useState('')
    const [isSavingRaca, setIsSavingRaca] = useState(false)

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

    // ============================================================================
    // Fetch Animals
    // ============================================================================

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
        listarRacas('', 'ATIVO').then(setRacasDisponiveis).catch(() => setRacasDisponiveis([]))
    }, [fetchAnimals])

    // ============================================================================
    // Aba Raças
    // ============================================================================

    const fetchRacas = useCallback(async (termo, statusFiltro) => {
        setIsLoadingRacas(true)
        setRacasFeedback({ type: '', message: '' })
        try {
            const lista = await listarRacas(termo ?? '', statusFiltro ?? 'ATIVO')
            setRacas(lista)
        } catch (error) {
            setRacasFeedback({ type: 'error', message: error.message || 'Falha ao carregar as raças.' })
        } finally {
            setIsLoadingRacas(false)
        }
    }, [])

    useEffect(() => {
        if (activeTab === 'racas') {
            fetchRacas(racaActiveSearch, racaStatusFiltro)
        }
    }, [activeTab, fetchRacas, racaActiveSearch, racaStatusFiltro])

    function handleRacaSearchSubmit(event) {
        event.preventDefault()
        setRacaActiveSearch(racaSearch.trim())
    }

    function handleRacaClearSearch() {
        setRacaSearch('')
        setRacaActiveSearch('')
    }

    function openCreateRacaModal() {
        setRacaFormMode('create')
        setRacaFormData(DEFAULT_RACA_FORM)
        setRacaFormFeedback('')
        setRacaModal({ open: true, raca: null })
    }

    function openEditRacaModal(raca) {
        setRacaFormMode('edit')
        setRacaFormFeedback('')
        setRacaFormData({ id: raca.id, nome: raca.nome, sigla: raca.sigla })
        setRacaModal({ open: true, raca })
    }

    function closeRacaModal() {
        setRacaModal({ open: false, raca: null })
        setRacaFormData(DEFAULT_RACA_FORM)
        setRacaFormFeedback('')
    }

    function handleRacaFormChange(event) {
        const { name, value } = event.target
        setRacaFormData((c) => ({ ...c, [name]: value }))
    }

    async function refreshRacasDisponiveis() {
        listarRacas('', 'ATIVO').then(setRacasDisponiveis).catch(() => {})
    }

    async function handleSubmitRacaForm(event) {
        event.preventDefault()
        setIsSavingRaca(true)
        setRacaFormFeedback('')
        try {
            if (racaFormMode === 'create') {
                await cadastrarRaca(currentUser.email, racaFormData)
                setRacasFeedback({ type: 'info', message: 'Raça cadastrada com sucesso.' })
            } else {
                await atualizarRaca(racaFormData.id, currentUser.email, racaFormData)
                setRacasFeedback({ type: 'info', message: 'Raça atualizada com sucesso.' })
            }
            closeRacaModal()
            await fetchRacas(racaActiveSearch, racaStatusFiltro)
            await refreshRacasDisponiveis()
        } catch (error) {
            setRacaFormFeedback(error.message || 'Falha ao salvar a raça.')
        } finally {
            setIsSavingRaca(false)
        }
    }

    async function handleInativarRaca(raca) {
        if (!window.confirm(`Deseja inativar a raça "${raca.nome}"?`)) return
        setRacasFeedback({ type: '', message: '' })
        try {
            await deletarRaca(raca.id, currentUser.email)
            setRacasFeedback({ type: 'info', message: 'Raça inativada com sucesso.' })
            await fetchRacas(racaActiveSearch, racaStatusFiltro)
            await refreshRacasDisponiveis()
        } catch (error) {
            setRacasFeedback({ type: 'error', message: error.message || 'Falha ao inativar a raça.' })
        }
    }

    async function handleReativarRaca(raca) {
        setRacasFeedback({ type: '', message: '' })
        try {
            await reativarRaca(raca.id, currentUser.email)
            setRacasFeedback({ type: 'info', message: 'Raça reativada com sucesso.' })
            await fetchRacas(racaActiveSearch, racaStatusFiltro)
            await refreshRacasDisponiveis()
        } catch (error) {
            setRacasFeedback({ type: 'error', message: error.message || 'Falha ao reativar a raça.' })
        }
    }

    // ============================================================================
    // Search & Filter Handlers
    // ============================================================================

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

    // ============================================================================
    // Modal Handlers
    // ============================================================================

    function closeModal() {
        setModal({ type: null, animal: null })
        setFormData(DEFAULT_FORM)
        setFormFeedback('')
    }

    function openCreateModal() {
        setFormMode('create')
        setFormData(DEFAULT_FORM)
        setFormFeedback('')
        setModal({ type: 'form', animal: null })
    }

    function openEditModal(animal) {
        setFormMode('edit')
        setFormFeedback('')
        setFormData({
            ...DEFAULT_FORM,
            ...animal,
            pesoAtual: String(animal.pesoAtual ?? ''),
        })
        setModal({ type: 'form', animal })
    }

    function openDetailsModal(animal) {
        setModal({ type: 'details', animal })
    }

    // ============================================================================
    // Form Handlers
    // ============================================================================

    function handleFormChange(event) {
        const { name, value } = event.target
        setFormData((prev) => ({ ...prev, [name]: value }))
    }

    // ============================================================================
    // Submit & Delete
    // ============================================================================

    async function handleSubmitForm(event) {
        event.preventDefault()
        setIsSaving(true)
        setFormFeedback('')
        setFeedback({ type: '', message: '' })

        try {
            const isCreate = formMode === 'create'

            // ALINHADO COM A API: Cadastrar passa email + dados, atualizar passa brinco + dados
            const result = isCreate
                ? await cadastrarAnimal(currentUser.email, formData)
                : await atualizarAnimal(formData.codigoBrinco, formData)

            if (isBackendErrorMessage(result)) {
                throw new Error(
                    getBackendMessage(result) ||
                    `Falha ao ${isCreate ? 'cadastrar' : 'atualizar'} animal.`
                )
            }

            setFeedback({
                type: 'info',
                message: `Animal ${isCreate ? 'cadastrado' : 'atualizado'} com sucesso.`,
            })

            closeModal()
            await fetchAnimals(activeSearch)
        } catch (error) {
            setFormFeedback(error.message || 'Falha ao salvar o animal.')
        } finally {
            setIsSaving(false)
        }
    }

    async function handleDelete(animal) {
        if (!window.confirm(`Deseja excluir o animal ${animal.codigoBrinco}?`)) {
            return
        }

        setIsDeleting(true)
        setFeedback({ type: '', message: '' })

        try {
            const result = await deletarAnimal(animal.codigoBrinco)

            if (isBackendErrorMessage(result)) {
                throw new Error(
                    getBackendMessage(result) || 'Falha ao excluir animal.'
                )
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

    // ============================================================================
    // Render
    // ============================================================================

    return (
        <main className="animals-layout">
            {/* Sidebar */}
            <aside className="animals-sidebar">
                <div className="animals-logo"><img src="/logo.png" alt="GADO" /></div>
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
                    {!['CUIDADOR', 'CUIDADOR_CHEFE'].includes(currentUser?.perfil) ? (
                        <button
                            type="button"
                            className="menu-item"
                            onClick={() => onNavigate('financeiro')}
                        >
                            Financeiro
                        </button>
                    ) : null}
                    <button
                        type="button"
                        className="menu-item"
                        onClick={() => onNavigate('perfil')}
                    >
                        Perfil
                    </button>
                    <button type="button" className="menu-item" onClick={() => onNavigate('tarefas')}>
                        Tarefas
                    </button>
                    {currentUser.perfil === 'ADMINISTRADOR' && (
                        <button
                            type="button"
                            className="menu-item"
                            onClick={() => onNavigate('configuracoes')}
                        >
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

            {/* Content */}
            <section className="animals-content">
                <header className="page-header">
                    <h1>Animais</h1>
                </header>

                <div className="insumos-tabs">
                    <button
                        type="button"
                        className={`insumos-tab ${activeTab === 'animais' ? 'insumos-tab--active' : ''}`}
                        onClick={() => setActiveTab('animais')}
                    >
                        Animais
                    </button>
                    <button
                        type="button"
                        className={`insumos-tab ${activeTab === 'racas' ? 'insumos-tab--active' : ''}`}
                        onClick={() => setActiveTab('racas')}
                    >
                        Raças
                    </button>
                </div>

                {activeTab === 'animais' ? (
                <>
                {/* Feedback */}
                {feedback.message ? (
                    <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
                        {feedback.message}
                    </p>
                ) : null}

                {/* Toolbar: busca + filtros + ações */}
                <div className="data-toolbar">
                    <form className="toolbar-search" onSubmit={handleSearchSubmit}>
                        <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
                        <input
                            type="text"
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            placeholder="Buscar por código do brinco"
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

                    <label className="toolbar-date-label">
                        <span className="toolbar-date-label__text">Nascimento de</span>
                        <input
                            type="date"
                            className="toolbar-date"
                            value={dateFrom}
                            onChange={(e) => {
                                setDateFrom(e.target.value)
                                setPage(0)
                            }}
                        />
                    </label>

                    <label className="toolbar-date-label">
                        <span className="toolbar-date-label__text">Nascimento até</span>
                        <input
                            type="date"
                            className="toolbar-date"
                            value={dateTo}
                            onChange={(e) => {
                                setDateTo(e.target.value)
                                setPage(0)
                            }}
                        />
                    </label>

                    <button
                        type="button"
                        className="btn-export-csv"
                        onClick={() => exportAnimaisCSV(filteredCards)}
                    >
                        Exportar CSV
                    </button>

                    <button type="button" className="btn-new-entity" onClick={openCreateModal}>
                        + Novo Animal
                    </button>
                </div>

                {/* Tabela */}
                <div className="data-table-wrapper">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Código Brinco</th>
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
                                    <td colSpan={7} className="table-loading">
                                        Carregando...
                                    </td>
                                </tr>
                            ) : paginatedCards.length === 0 ? (
                                <tr>
                                    <td colSpan={7} className="table-empty">
                                        {activeSearch
                                            ? `Nenhum resultado para "${activeSearch}".`
                                            : 'Nenhum animal cadastrado. Clique em "+ Novo Animal" para começar.'}
                                    </td>
                                </tr>
                            ) : (
                                paginatedCards.map((animal) => (
                                    <tr key={animal.codigoBrinco}>
                                        <td className="td-mono">{animal.codigoBrinco}</td>
                                        <td>{animal.racaNome || '—'}</td>
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
                </>
                ) : (
                <>
                {racasFeedback.message ? (
                    <p className={`feedback ${racasFeedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
                        {racasFeedback.message}
                    </p>
                ) : null}

                <div className="data-toolbar">
                    <form className="toolbar-search" onSubmit={handleRacaSearchSubmit}>
                        <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
                        <input
                            type="text"
                            value={racaSearch}
                            onChange={(e) => setRacaSearch(e.target.value)}
                            placeholder="Buscar raça por nome"
                        />
                        {racaActiveSearch ? (
                            <button
                                type="button"
                                className="toolbar-search__clear"
                                onClick={handleRacaClearSearch}
                                aria-label="Limpar busca"
                            >
                                ✕
                            </button>
                        ) : null}
                    </form>

                    <select
                        className="toolbar-select"
                        value={racaStatusFiltro}
                        onChange={(e) => setRacaStatusFiltro(e.target.value)}
                    >
                        <option value="ATIVO">Ativas</option>
                        <option value="INATIVO">Inativas</option>
                        <option value="TODOS">Todas</option>
                    </select>

                    <button type="button" className="btn-new-entity" onClick={openCreateRacaModal}>
                        + Nova Raça
                    </button>
                </div>

                <div className="data-table-wrapper">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Sigla</th>
                                <th>Nome</th>
                                <th>Produto vinculado</th>
                                <th>Cabeças (saldo)</th>
                                <th>Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            {isLoadingRacas ? (
                                <tr>
                                    <td colSpan={5} className="table-loading">Carregando...</td>
                                </tr>
                            ) : racas.length === 0 ? (
                                <tr>
                                    <td colSpan={5} className="table-empty">
                                        Nenhuma raça cadastrada. Clique em "+ Nova Raça" para começar.
                                    </td>
                                </tr>
                            ) : (
                                racas.map((raca) => (
                                    <tr key={raca.id}>
                                        <td className="codigo-produto">{raca.sigla}</td>
                                        <td>
                                            {raca.nome}
                                            {raca.status === 'INATIVO' ? (
                                                <span className="setor-badge setor-badge--inativo estoque-badge">Inativa</span>
                                            ) : null}
                                        </td>
                                        <td>{raca.produtoNome || '—'} {raca.produtoCodigo ? `(${raca.produtoCodigo})` : ''}</td>
                                        <td>{raca.saldoAtual ?? 0}</td>
                                        <td>
                                            <div className="row-actions">
                                                {raca.status === 'INATIVO' ? (
                                                    <button type="button" className="btn-row" onClick={() => handleReativarRaca(raca)}>
                                                        Reativar
                                                    </button>
                                                ) : (
                                                    <>
                                                        <button type="button" className="btn-row btn-row--edit" onClick={() => openEditRacaModal(raca)}>
                                                            Editar
                                                        </button>
                                                        <button type="button" className="btn-row btn-row--danger" onClick={() => handleInativarRaca(raca)}>
                                                            Inativar
                                                        </button>
                                                    </>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
                </>
                )}
            </section>

            {/* Modals */}
            {modal.type === 'form' && (
                <AnimalFormModal
                    mode={formMode}
                    formData={formData}
                    isSaving={isSaving}
                    feedback={formFeedback}
                    userEmail={currentUser.email}
                    racasDisponiveis={racasDisponiveis}
                    onClose={closeModal}
                    onChange={handleFormChange}
                    onSubmit={handleSubmitForm}
                />
            )}

            {racaModal.open && (
                <RacaFormModal
                    mode={racaFormMode}
                    formData={racaFormData}
                    isSaving={isSavingRaca}
                    feedback={racaFormFeedback}
                    onClose={closeRacaModal}
                    onChange={handleRacaFormChange}
                    onSubmit={handleSubmitRacaForm}
                />
            )}

            {modal.type === 'details' && modal.animal && (
                <AnimalDetailsModal
                    animal={toCardAnimal(modal.animal)}
                    onClose={closeModal}
                    onEdit={() => openEditModal(modal.animal)}
                    onDelete={handleDelete}
                    isDeleting={isDeleting}
                />
            )}
        </main>
    )
}

export default AnimaisPage
