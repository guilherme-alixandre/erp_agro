import { useCallback, useEffect, useMemo, useState } from 'react'
import AnimalFormModal from '../components/AnimalFormModal'
import AnimalDetailsModal from '../components/AnimalDetailsModal'
import {
    buscarAnimais,
    cadastrarAnimal,
    atualizarAnimal,
    deletarAnimal,
    getBackendMessage,
    isBackendErrorMessage,
} from '../integration/animalApi.js'
import { listarVacinas } from '../../insumos/integration/insumoApi.js'
import '../styles/animais.css'

// ============================================================================
// Default Form
// ============================================================================

const DEFAULT_FORM = {
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

function exportAnimaisCSV(animais) {
    const headers = ['Código Brinco', 'Nome', 'Raça', 'Sexo', 'Peso (KG)', 'Nascimento', 'Status']
    const rows = animais.map((a) => [
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

// ============================================================================
// Component
// ============================================================================

function AnimaisPage({ currentUser, onNavigate, onLogout }) {
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
    const [vacinasDisponiveis, setVacinasDisponiveis] = useState([])

    // Estado: Modal
    const [modal, setModal] = useState({ type: null, animal: null })
    const [formMode, setFormMode] = useState('create')
    const [formData, setFormData] = useState(DEFAULT_FORM)

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
    }, [fetchAnimals])

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
        carregarVacinasDisponiveis()
    }

    function openEditModal(animal) {
        setFormMode('edit')
        setFormFeedback('')
        setFormData({
            ...DEFAULT_FORM,
            ...animal,
            pesoAtual: String(animal.pesoAtual ?? ''),
            vacinas: animal.vacinas || [], // CORRIGIDO: Agora mantém as vacinas vindas do backend na edição
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

    function handleAddVacina() {
        setFormData((prev) => ({
            ...prev,
            vacinas: [...(prev.vacinas ?? []), { nome: '', dataOcorrencia: '' }],
        }))
    }

    function handleChangeVacina(index, field, value) {
        setFormData((prev) => {
            const vacinas = [...(prev.vacinas ?? [])]
            vacinas[index] = { ...vacinas[index], [field]: value }
            return { ...prev, vacinas }
        })
    }

    function handleRemoveVacina(index) {
        setFormData((prev) => ({
            ...prev,
            vacinas: (prev.vacinas ?? []).filter((_, i) => i !== index),
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
                        <button type="button" className="menu-item">
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

            {/* Modals */}
            {modal.type === 'form' && (
                <AnimalFormModal
                    mode={formMode}
                    formData={formData}
                    isSaving={isSaving}
                    feedback={formFeedback}
                    userEmail={currentUser.email}
                    vacinasDisponiveis={vacinasDisponiveis}
                    onClose={closeModal}
                    onChange={handleFormChange}
                    onSubmit={handleSubmitForm}
                    onAddVacina={handleAddVacina}
                    onChangeVacina={handleChangeVacina}
                    onRemoveVacina={handleRemoveVacina}
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
