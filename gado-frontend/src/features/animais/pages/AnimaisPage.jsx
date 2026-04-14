import { useMemo, useState } from 'react'
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
  testarCrudAnimal,
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

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const BRINCO_REGEX = /^[A-Za-z0-9-]+$/
const ALLOWED_SEX = ['M', 'F']
const ALLOWED_STATUS = ['EX1', 'EX2']

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

function validateSearch(brinco) {
  const cleanBrinco = brinco.trim()

  if (!cleanBrinco) {
    return 'Informe o brinco para buscar.'
  }

  if (!BRINCO_REGEX.test(cleanBrinco)) {
    return 'O brinco aceita apenas letras, números e hífen.'
  }

  return ''
}

function validateForm(data, mode) {
  if (mode === 'create') {
    if (!data.emailUsuario.trim()) {
      return 'Informe o e-mail do usuário.'
    }

    if (!EMAIL_REGEX.test(data.emailUsuario.trim())) {
      return 'Informe um e-mail válido.'
    }
  }

  const brinco = data.codigoBrinco.trim()
  if (!brinco) {
    return 'Informe o código do brinco.'
  }
  if (!BRINCO_REGEX.test(brinco)) {
    return 'O código do brinco aceita apenas letras, números e hífen.'
  }

  const nome = data.nome.trim()
  if (!nome || nome.length < 2) {
    return 'Informe um nome válido com pelo menos 2 caracteres.'
  }

  if (!data.dataNascimento) {
    return 'Informe a data de nascimento.'
  }

  const today = new Date().toISOString().slice(0, 10)
  if (data.dataNascimento > today) {
    return 'A data de nascimento não pode ser futura.'
  }

  const peso = Number(data.pesoAtual)
  if (!Number.isFinite(peso) || peso <= 0) {
    return 'Informe um peso maior que zero.'
  }

  if (!data.raca.trim()) {
    return 'Informe a raça.'
  }

  if (!data.cor.trim()) {
    return 'Informe a cor.'
  }

  if (!data.tamanho.trim()) {
    return 'Informe o tamanho.'
  }

  if (!ALLOWED_SEX.includes(data.sexo)) {
    return 'Sexo inválido.'
  }

  if (!ALLOWED_STATUS.includes(data.statusAnimal)) {
    return 'Status inválido.'
  }

  return ''
}

function AnimaisPage() {
  const [search, setSearch] = useState('')
  const [isLoadingSearch, setIsLoadingSearch] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [isTestingCrud, setIsTestingCrud] = useState(false)
  const [crudEmail, setCrudEmail] = useState('')
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
    const searchError = validateSearch(brinco)
    if (searchError) {
      setFeedback({ type: 'error', message: searchError })
      return
    }

    setIsLoadingSearch(true)
    setFeedback({ type: '', message: '' })
    try {
      const animal = await buscarAnimalPorBrinco(brinco)
      setAnimals((current) => mergeByBrinco(current, animal))
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao buscar animal.',
      })
    } finally {
      setIsLoadingSearch(false)
    }
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    const formError = validateForm(formData, formMode)
    if (formError) {
      setFormFeedback(formError)
      return
    }

    setIsSaving(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    try {
      if (formMode === 'create') {
        const result = await cadastrarAnimal(formData.emailUsuario.trim(), formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(getBackendMessage(result) || 'Falha ao cadastrar animal.')
        }

        const loadedAnimal = await buscarAnimalPorBrinco(formData.codigoBrinco)
        setAnimals((current) => mergeByBrinco(current, loadedAnimal))
        setFeedback({ type: 'info', message: 'Animal cadastrado com sucesso.' })
      } else {
        const result = await atualizarAnimal(formData.codigoBrinco, formData)
        if (isBackendErrorMessage(result)) {
          throw new Error(getBackendMessage(result) || 'Falha ao atualizar animal.')
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
        setFeedback({ type: 'info', message: 'Animal atualizado com sucesso.' })
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
    setFeedback({ type: '', message: '' })
    try {
      const result = await deletarAnimal(animal.codigoBrinco)
      if (isBackendErrorMessage(result)) {
        throw new Error(getBackendMessage(result) || 'Falha ao excluir animal.')
      }

      setAnimals((current) =>
        current.filter((item) => item.codigoBrinco !== animal.codigoBrinco),
      )
      closeModal()
      setFeedback({ type: 'info', message: 'Animal excluído com sucesso.' })
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao excluir animal.',
      })
    } finally {
      setIsDeleting(false)
    }
  }

  async function handleCrudTest() {
    const email = crudEmail.trim()
    if (!EMAIL_REGEX.test(email)) {
      setFeedback({
        type: 'error',
        message: 'Informe um e-mail válido para executar o teste CRUD.',
      })
      return
    }

    setIsTestingCrud(true)
    setFeedback({ type: '', message: '' })
    try {
      const passos = await testarCrudAnimal(email)
      setFeedback({
        type: 'info',
        message: `Conexão front↔back validada nos endpoints: ${passos.join(', ')}.`,
      })
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha no teste de conexão CRUD.',
      })
    } finally {
      setIsTestingCrud(false)
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

        <div className="animals-crud-test">
          <input
            type="email"
            value={crudEmail}
            onChange={(event) => setCrudEmail(event.target.value)}
            placeholder="E-mail do usuário para teste CRUD"
          />
          <button
            type="button"
            onClick={handleCrudTest}
            disabled={isTestingCrud}
          >
            {isTestingCrud ? 'Testando CRUD...' : 'Testar conexão CRUD'}
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

export default AnimaisPage
