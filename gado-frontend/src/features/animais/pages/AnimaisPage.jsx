import { useCallback, useEffect, useMemo, useState } from 'react'
import AnimalCard from '../components/AnimalCard'
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

  const cards = useMemo(() => animals.map(toCardAnimal), [animals])

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

  function closeModal() {
    setModal({ type: null, animal: null })
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

  function openCreateModal() {
    setFormMode('create')
    setFormData(defaultForm)
    setFormFeedback('')
    setModal({ type: 'form', animal: null })
    carregarVacinasDisponiveis()
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
    setIsSaving(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    try {
      if (formMode === 'create') {
        const result = await cadastrarAnimal(currentUser.email, formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(getBackendMessage(result) || 'Falha ao cadastrar animal.')
        }
        setFeedback({ type: 'info', message: 'Animal cadastrado com sucesso.' })
      } else {
        const result = await atualizarAnimal(formData.codigoBrinco, formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(getBackendMessage(result) || 'Falha ao atualizar animal.')
        }
        setFeedback({ type: 'info', message: 'Animal atualizado com sucesso.' })
      }

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
        <header className="animals-header">
          <h1>Animais</h1>
          <span>{currentUser.email}</span>
        </header>

        <form className="animals-search" onSubmit={handleSearchSubmit}>
          <input
            type="text"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Buscar por nome ou código do brinco"
          />
          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Buscando...' : 'Buscar'}
          </button>
          {activeSearch ? (
            <button type="button" onClick={handleClearSearch} disabled={isLoading}>
              Limpar
            </button>
          ) : null}
        </form>

        <p className="animals-count">
          {isLoading
            ? 'Carregando...'
            : activeSearch
              ? `${cards.length} ${cards.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
              : `${cards.length} ${cards.length === 1 ? 'animal cadastrado' : 'animais cadastrados'}`}
        </p>

        {feedback.message ? (
          <p
            className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
          >
            {feedback.message}
          </p>
        ) : null}

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
                <span>
                  Nenhum resultado para {`"${activeSearch}"`}. Ajuste o termo da busca.
                </span>
              </>
            ) : (
              <>
                <p>Nenhum animal cadastrado.</p>
                <span>Clique no botão + para cadastrar o primeiro animal.</span>
              </>
            )}
          </div>
        )}

        <button
          type="button"
          className="fab-add"
          aria-label="Adicionar animal"
          onClick={openCreateModal}
        >
          +
        </button>
      </section>

      {modal.type === 'form' ? (
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
