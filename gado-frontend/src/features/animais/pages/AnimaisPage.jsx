import { useMemo, useState } from 'react'
import AnimalCard from '../components/AnimalCard'
import AnimalFormModal from '../components/AnimalFormModal'
import AnimalDetailsModal from '../components/AnimalDetailsModal'
import {
  atualizarAnimal,
  buscarAnimalPorBrinco,
  cadastrarAnimal,
  deletarAnimal,
  isBackendErrorMessage,
} from '../../../services/animalApi'
import '../styles/animais.css'

const defaultForm = {
  emailUsuario: '',
  codigoBrinco: '',
  nome: '',
  dataNascimento: '',
  pesoAtual: '',
  raca: '',
  cor: '',
  tamanho: '',
  sexo: 'M',
  statusAnimal: 'EX1',
}

function calcAgeLabel(dateText) {
  if (!dateText) return 'idade não informada'
  const birthDate = new Date(`${dateText}T00:00:00`)
  const now = new Date()
  let years = now.getFullYear() - birthDate.getFullYear()
  const monthDiff = now.getMonth() - birthDate.getMonth()
  const dayDiff = now.getDate() - birthDate.getDate()
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

function AnimalPage() {
  const [search, setSearch] = useState('')
  const [isLoadingSearch, setIsLoadingSearch] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState('')
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
    setFormFeedback('')
    setFormData({
      ...defaultForm,
      ...animal,
      pesoAtual: String(animal.pesoAtual ?? ''),
    })
    setModal({ type: 'form', animal })
  }

  function openDetailsModal(animal) {
    setModal({ type: 'details', animal })
  }

  async function handleSearch(event) {
    event.preventDefault()
    const brinco = search.trim()
    if (!brinco) return

    setIsLoadingSearch(true)
    setFeedback('')
    try {
      const animal = await buscarAnimalPorBrinco(brinco)
      setAnimals((current) => mergeByBrinco(current, animal))
    } catch (error) {
      setFeedback(error.message || 'Falha ao buscar animal.')
    } finally {
      setIsLoadingSearch(false)
    }
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormFeedback('')
    setFeedback('')

    try {
      if (formMode === 'create') {
        const result = await cadastrarAnimal(formData.emailUsuario, formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(result)
        }

        const loadedAnimal = await buscarAnimalPorBrinco(formData.codigoBrinco)
        setAnimals((current) => mergeByBrinco(current, loadedAnimal))
        setFeedback('Animal cadastrado com sucesso.')
      } else {
        const result = await atualizarAnimal(formData.codigoBrinco, formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(result)
        }

        setAnimals((current) =>
          current.map((animal) =>
            animal.codigoBrinco === formData.codigoBrinco
              ? {
                  ...animal,
                  ...formData,
                  pesoAtual: Number(formData.pesoAtual),
                }
              : animal,
          ),
        )
        setFeedback('Animal atualizado com sucesso.')
      }

      closeModal()
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
    setFeedback('')
    try {
      const result = await deletarAnimal(animal.codigoBrinco)
      if (isBackendErrorMessage(result)) {
        throw new Error(result)
      }

      setAnimals((current) =>
        current.filter((item) => item.codigoBrinco !== animal.codigoBrinco),
      )
      closeModal()
      setFeedback('Animal excluído com sucesso.')
    } catch (error) {
      setFeedback(error.message || 'Falha ao excluir animal.')
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
          <button type="button" className="menu-item">
            Lotes
          </button>
          <button type="button" className="menu-item">
            Setores
          </button>
          <button type="button" className="menu-item">
            Insumos
          </button>
          <button type="button" className="menu-item">
            Financeiro
          </button>
          <button type="button" className="menu-item">
            Perfil
          </button>
        </nav>
      </aside>

      <section className="animals-content">
        <header className="animals-header">
          <h1>Animais</h1>
          <span>Perfil</span>
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

        {feedback ? <p className="feedback feedback--info">{feedback}</p> : null}

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
      </section>

      {modal.type === 'form' ? (
        <AnimalFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          feedback={formFeedback}
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
    </main>
  )
}

export default AnimalPage
