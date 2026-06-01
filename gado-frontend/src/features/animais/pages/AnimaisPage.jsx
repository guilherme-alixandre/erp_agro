import { useState, useMemo } from 'react'
import AnimalCard from '../components/AnimalCard'
import AnimalFormModal from '../components/AnimalFormModal'
import AnimalDetailsModal from '../components/AnimalDetailsModal'
import {
  atualizarAnimal,
  buscarAnimalPorBrinco,
  cadastrarAnimal,
  deletarAnimal,
  getBackendMessage,
  isBackendErrorMessage,
} from '../../../services/animalApi'
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

function mergeByBrinco(current, animal) {
  const index = current.findIndex(
    (item) => item.codigoBrinco === animal.codigoBrinco,
  )
  if (index === -1) return [animal, ...current]
  const next = [...current]
  next[index] = animal
  return next
}

function AnimaisPage({ currentUser }) {
  const [search, setSearch] = useState('')
  const [isLoadingSearch, setIsLoadingSearch] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [animals, setAnimals] = useState([])
  const [modal, setModal] = useState({ type: null, animal: null })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultForm)
  const [formFeedback, setFormFeedback] = useState('')

  const cards = useMemo(() => animals.map(toCardAnimal), [animals])

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

  function openCreateModal() {
    setFormMode('create')
    setFormData(defaultForm)
    setFormFeedback('')
    setModal({ type: 'form', animal: null })
  }

  function openEditModal(animal) {
    setFormMode('edit')
    setFormData(animal)
    setFormFeedback('')
    setModal({ type: 'form', animal })
  }

  function openDetailsModal(animal) {
    setModal({ type: 'details', animal })
  }

  async function handleSearch(event) {
    event.preventDefault()
    if (!search.trim()) {
      setFeedback({ type: '', message: '' })
      setAnimals([])
      return
    }

    setIsLoadingSearch(true)
    setFeedback({ type: '', message: '' })

    try {
      const animal = await buscarAnimalPorBrinco(search.trim())
      setAnimals([animal])
      setFeedback({
        type: 'info',
        message: `Animal encontrado: ${animal.nome}`,
      })
    } catch (error) {
      const message = isBackendErrorMessage(error.message)
        ? getBackendMessage(error.message)
        : error.message || 'Animal não encontrado.'
      setFeedback({
        type: 'error',
        message,
      })
      setAnimals([])
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
        ? await cadastrarAnimal({
            ...formData,
            usuario_id: currentUser.id,
          })
        : await atualizarAnimal(formData.codigoBrinco, formData)

      const isError = isBackendErrorMessage(payload)
      if (isError) {
        throw new Error(getBackendMessage(payload))
      }

      if (formMode === 'create') {
        setAnimals((current) => mergeByBrinco(current, payload))
        setFeedback({
          type: 'info',
          message: 'Animal cadastrado com sucesso!',
        })
      } else {
        setAnimals((current) => mergeByBrinco(current, payload))
        setFeedback({
          type: 'info',
          message: 'Animal atualizado com sucesso!',
        })
      }

      closeModal()
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar animal.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete(animal) {
    if (!window.confirm(`Deseja excluir o animal "${animal.nome}"?`)) {
      return
    }

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })

    try {
      await deletarAnimal(animal.codigoBrinco)
      setAnimals((current) =>
        current.filter((item) => item.codigoBrinco !== animal.codigoBrinco)
      )
      setFeedback({
        type: 'info',
        message: 'Animal excluído com sucesso!',
      })
      closeModal()
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
    <section className="animals-content">
      <header className="animals-header">
        <h1>Animais</h1>
        <span>{currentUser.email}</span>
      </header>

      <form className="animals-search" onSubmit={handleSearch}>
        <input
          type="text"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Pesquisar animal, lote, setor ou etiqueta"
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
          <p>Nenhum animal carregado.</p>
          <span>
            Busque por brinco para carregar um animal ou clique no botão +
            para cadastrar.
          </span>
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

      {modal.type === 'form' ? (
        <AnimalFormModal
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

      {modal.type === 'details' && modal.animal ? (
        <AnimalDetailsModal
          animal={toCardAnimal(modal.animal)}
          onClose={closeModal}
          onEdit={() => openEditModal(modal.animal)}
          onDelete={handleDelete}
          isDeleting={isDeleting}
        />
      ) : null}
    </section>
  )
}

export default AnimaisPage
