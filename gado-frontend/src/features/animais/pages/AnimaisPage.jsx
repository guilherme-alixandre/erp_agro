import { useCallback, useEffect, useMemo, useState } from 'react'
import AnimalCard from '../components/AnimalCard'
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

// ============================================================================
// Component
// ============================================================================

function AnimaisPage({ currentUser, onNavigate, onLogout }) {
    // Estado: Busca
    const [search, setSearch] = useState('')
    const [activeSearch, setActiveSearch] = useState('')

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
    // Search Handlers
    // ============================================================================

    function handleSearchSubmit(event) {
        event.preventDefault()
        const termo = search.trim()
        setActiveSearch(termo)
        fetchAnimals(termo)
    }

    function handleClearSearch() {
        setSearch('')
        setActiveSearch('')
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
                <div className="animals-logo">🌿</div>
                <nav>
                    <button type="button" className="menu-item menu-item--active">
                        Animais
                    </button>
                    <button type="button" className="menu-item">
                        Lotes
                    </button>
                    <button type="button" className="menu-item">
                        Setores
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
                <header className="animals-header">
                    <h1>Animais</h1>
                    <span>{currentUser.email}</span>
                </header>

                {/* Search */}
                <form className="animals-search" onSubmit={handleSearchSubmit}>
                    <input
                        type="text"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder="Buscar por nome ou código do brinco"
                    />
                    <button type="submit" disabled={isLoading}>
                        {isLoading ? 'Buscando...' : 'Buscar'}
                    </button>
                    {activeSearch && (
                        <button type="button" onClick={handleClearSearch} disabled={isLoading}>
                            Limpar
                        </button>
                    )}
                </form>

                {/* Count */}
                <p className="animals-count">
                    {isLoading
                        ? 'Carregando...'
                        : activeSearch
                            ? `${cards.length} ${cards.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
                            : `${cards.length} ${cards.length === 1 ? 'animal cadastrado' : 'animais cadastrados'}`}
                </p>

                {/* Feedback */}
                {feedback.message && (
                    <p className={`feedback feedback--${feedback.type === 'error' ? 'error' : 'info'}`}>
                        {feedback.message}
                    </p>
                )}

                {/* Grid or Empty State */}
                {cards.length ? (
                    <div className="animals-grid">
                        {cards.map((animal) => (
                            <AnimalCard
                                key={animal.codigoBrinco}
                                animal={animal}
                                onDetalhes={openDetailsModal}
                                onEditar={openEditModal}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="animals-empty">
                        {activeSearch ? (
                            <>
                                <p>Nenhum animal encontrado.</p>
                                <span>Nenhum resultado para "{activeSearch}". Ajuste o termo da busca.</span>
                            </>
                        ) : (
                            <>
                                <p>Nenhum animal cadastrado.</p>
                                <span>Clique no botão + para cadastrar o primeiro animal.</span>
                            </>
                        )}
                    </div>
                )}

                {/* FAB Button */}
                <button
                    type="button"
                    className="fab-add"
                    aria-label="Adicionar animal"
                    onClick={openCreateModal}
                >
                    +
                </button>
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