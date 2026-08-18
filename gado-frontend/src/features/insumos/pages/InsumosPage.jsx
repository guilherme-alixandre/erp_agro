import { useCallback, useEffect, useState } from 'react'
import VacinaCard from '../components/VacinaCard'
import VacinaFormModal from '../components/VacinaFormModal'
import InsumoEstoqueFormModal from '../components/InsumoEstoqueFormModal'
import EntradaEstoqueModal from '../components/EntradaEstoqueModal'
import AlimentarLoteTab from '../components/AlimentarLoteTab'
import {
  atualizarVacina,
  cadastrarVacina,
  confirmarVacina,
  deletarVacina,
  listarVacinas,
  listarEstoque,
  cadastrarInsumoEstoque,
  atualizarInsumoEstoque,
  registrarEntradaEstoque,
} from '../integration/insumoApi'
import { listarUnidadesMedida } from '../integration/unidadeMedidaApi'
import '../../animais/styles/animais.css'
import '../styles/insumos.css'

const PERFIS_GESTAO_ESTOQUE = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR_CHEFE']

const defaultVacinaForm = {
  id: null,
  nome: '',
  pendente: false,
}

const defaultEstoqueForm = {
  nome: '',
  tipo: 'RACAO',
  unidadeMedidaPrimariaId: '',
  unidadeMedidaPrimariaSigla: '',
  unidadeMedidaSecundariaId: '',
  fatorConversao: '',
  estoqueMinimo: '',
  precoCompraMedio: '',
  precoUltimaCompra: '',
}

const defaultEntradaForm = {
  quantidade: '',
  precoUnitario: '',
  numeroNf: '',
  chaveAcessoNf: '',
  dataEntrada: '',
}

function InsumosPage({ currentUser, onNavigate, onLogout }) {
  const [activeTab, setActiveTab] = useState('vacinas')
  const canGerenciarEstoque = PERFIS_GESTAO_ESTOQUE.includes(currentUser?.perfil)

  // ── Vacinas (existente) ────────────────────────────────────────────
  const [search, setSearch] = useState('')
  const [activeSearch, setActiveSearch] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [vacinas, setVacinas] = useState([])
  const [modal, setModal] = useState({ open: false })
  const [formMode, setFormMode] = useState('create')
  const [formData, setFormData] = useState(defaultVacinaForm)
  const [formFeedback, setFormFeedback] = useState('')

  const fetchVacinas = useCallback(async (termo) => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const list = await listarVacinas(termo)
      setVacinas(list)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar vacinas.',
      })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchVacinas('')
  }, [fetchVacinas])

  function handleSearchSubmit(event) {
    event.preventDefault()
    const termo = search.trim()
    setActiveSearch(termo)
    fetchVacinas(termo)
  }

  function handleClearSearch() {
    setSearch('')
    setActiveSearch('')
    fetchVacinas('')
  }

  function closeModal() {
    setModal({ open: false })
    setFormData(defaultVacinaForm)
    setFormFeedback('')
  }

  function handleFormChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({ ...current, [name]: value }))
  }

  function openCreateModal() {
    setFormMode('create')
    setFormData(defaultVacinaForm)
    setFormFeedback('')
    setModal({ open: true })
  }

  function openEditModal(vacina) {
    setFormMode('edit')
    setFormFeedback('')
    setFormData({
      id: vacina.id,
      nome: vacina.nome,
      pendente: vacina.pendente === true,
    })
    setModal({ open: true })
  }

  async function handleSubmitForm(event) {
    event.preventDefault()
    setIsSaving(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })

    try {
      if (formMode === 'create') {
        await cadastrarVacina({ nome: formData.nome, pendente: false })
        setFeedback({ type: 'info', message: 'Vacina cadastrada com sucesso.' })
      } else {
        await atualizarVacina(formData.id, { nome: formData.nome })
        setFeedback({ type: 'info', message: 'Vacina atualizada com sucesso.' })
      }
      closeModal()
      await fetchVacinas(activeSearch)
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao salvar vacina.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleConfirmar() {
    if (!formData.id) return
    setIsSaving(true)
    setFormFeedback('')
    try {
      await confirmarVacina(formData.id)
      setFeedback({
        type: 'info',
        message: 'Vacina confirmada com sucesso.',
      })
      closeModal()
      await fetchVacinas(activeSearch)
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao confirmar vacina.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDelete() {
    if (!formData.id) return
    const confirmDelete = window.confirm(
      `Deseja excluir a vacina "${formData.nome}"?`,
    )
    if (!confirmDelete) return

    setIsDeleting(true)
    setFormFeedback('')
    setFeedback({ type: '', message: '' })
    try {
      await deletarVacina(formData.id)
      setFeedback({ type: 'info', message: 'Vacina excluída com sucesso.' })
      closeModal()
      await fetchVacinas(activeSearch)
    } catch (error) {
      setFormFeedback(error.message || 'Falha ao excluir vacina.')
    } finally {
      setIsDeleting(false)
    }
  }

  // ── Estoque (novo) ────────────────────────────────────────────────

  const [estoqueSearch, setEstoqueSearch] = useState('')
  const [estoqueActiveSearch, setEstoqueActiveSearch] = useState('')
  const [isLoadingEstoque, setIsLoadingEstoque] = useState(false)
  const [estoqueFeedback, setEstoqueFeedback] = useState({ type: '', message: '' })
  const [insumosEstoque, setInsumosEstoque] = useState([])
  const [unidades, setUnidades] = useState([])
  const [estoqueModal, setEstoqueModal] = useState({ type: null, insumo: null })
  const [estoqueFormMode, setEstoqueFormMode] = useState('create')
  const [estoqueFormData, setEstoqueFormData] = useState(defaultEstoqueForm)
  const [estoqueFormFeedback, setEstoqueFormFeedback] = useState('')
  const [isSavingEstoque, setIsSavingEstoque] = useState(false)
  const [entradaFormData, setEntradaFormData] = useState(defaultEntradaForm)
  const [entradaFeedback, setEntradaFeedback] = useState('')
  const [isSavingEntrada, setIsSavingEntrada] = useState(false)

  const fetchEstoque = useCallback(async (termo) => {
    setIsLoadingEstoque(true)
    setEstoqueFeedback({ type: '', message: '' })
    try {
      const list = await listarEstoque(termo)
      setInsumosEstoque(list)
    } catch (error) {
      setEstoqueFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar o estoque.',
      })
    } finally {
      setIsLoadingEstoque(false)
    }
  }, [])

  useEffect(() => {
    fetchEstoque('')
    listarUnidadesMedida()
      .then(setUnidades)
      .catch(() => setUnidades([]))
  }, [fetchEstoque])

  function handleEstoqueSearchSubmit(event) {
    event.preventDefault()
    const termo = estoqueSearch.trim()
    setEstoqueActiveSearch(termo)
    fetchEstoque(termo)
  }

  function handleEstoqueClearSearch() {
    setEstoqueSearch('')
    setEstoqueActiveSearch('')
    fetchEstoque('')
  }

  function closeEstoqueModal() {
    setEstoqueModal({ type: null, insumo: null })
    setEstoqueFormData(defaultEstoqueForm)
    setEstoqueFormFeedback('')
    setEntradaFormData(defaultEntradaForm)
    setEntradaFeedback('')
  }

  function handleEstoqueFormChange(event) {
    const { name, value } = event.target
    setEstoqueFormData((current) => ({ ...current, [name]: value }))
  }

  function handleEntradaFormChange(event) {
    const { name, value } = event.target
    setEntradaFormData((current) => ({ ...current, [name]: value }))
  }

  function openCreateEstoqueModal() {
    setEstoqueFormMode('create')
    setEstoqueFormData(defaultEstoqueForm)
    setEstoqueFormFeedback('')
    setEstoqueModal({ type: 'form', insumo: null })
  }

  function openEditEstoqueModal(insumo) {
    setEstoqueFormMode('edit')
    setEstoqueFormFeedback('')
    setEstoqueFormData({
      nome: insumo.nome,
      tipo: insumo.tipo,
      unidadeMedidaPrimariaId: insumo.unidadeMedidaPrimariaId ?? '',
      unidadeMedidaPrimariaSigla: insumo.unidadeMedidaPrimariaSigla,
      unidadeMedidaSecundariaId: insumo.unidadeMedidaSecundariaId ?? '',
      fatorConversao: insumo.fatorConversao ?? '',
      estoqueMinimo: insumo.estoqueMinimo ?? '',
      precoCompraMedio: insumo.precoCompraMedio ?? '',
      precoUltimaCompra: insumo.precoUltimaCompra ?? '',
    })
    setEstoqueModal({ type: 'form', insumo })
  }

  function openEntradaModal(insumo) {
    setEntradaFormData({ ...defaultEntradaForm })
    setEntradaFeedback('')
    setEstoqueModal({ type: 'entrada', insumo })
  }

  async function handleSubmitEstoqueForm(event) {
    event.preventDefault()
    setIsSavingEstoque(true)
    setEstoqueFormFeedback('')
    setEstoqueFeedback({ type: '', message: '' })
    try {
      if (estoqueFormMode === 'create') {
        await cadastrarInsumoEstoque(currentUser.email, estoqueFormData)
        setEstoqueFeedback({ type: 'info', message: 'Insumo cadastrado com sucesso.' })
      } else {
        await atualizarInsumoEstoque(estoqueModal.insumo.id, currentUser.email, estoqueFormData)
        setEstoqueFeedback({ type: 'info', message: 'Insumo atualizado com sucesso.' })
      }
      closeEstoqueModal()
      await fetchEstoque(estoqueActiveSearch)
    } catch (error) {
      setEstoqueFormFeedback(error.message || 'Falha ao salvar o insumo.')
    } finally {
      setIsSavingEstoque(false)
    }
  }

  async function handleSubmitEntrada(event) {
    event.preventDefault()
    setIsSavingEntrada(true)
    setEntradaFeedback('')
    setEstoqueFeedback({ type: '', message: '' })
    try {
      await registrarEntradaEstoque(estoqueModal.insumo.id, currentUser.email, entradaFormData)
      setEstoqueFeedback({ type: 'info', message: 'Entrada de estoque registrada com sucesso.' })
      closeEstoqueModal()
      await fetchEstoque(estoqueActiveSearch)
    } catch (error) {
      setEntradaFeedback(error.message || 'Falha ao registrar a entrada de estoque.')
    } finally {
      setIsSavingEntrada(false)
    }
  }

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo"><img src="/logo.png" alt="GADO" /></div>
        <nav>
          <button
            type="button"
            className="menu-item"
            onClick={() => onNavigate('animais')}
          >
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
          <button type="button" className="menu-item menu-item--active">
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
          <h1>Insumos</h1>
          <span>{currentUser.email}</span>
        </header>

        <div className="insumos-tabs">
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'vacinas' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('vacinas')}
          >
            Vacinas
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'estoque' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('estoque')}
          >
            Estoque
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'alimentar' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('alimentar')}
          >
            Alimentar Lote
          </button>
        </div>

        {activeTab === 'vacinas' ? (
          <>
            <form className="animals-search" onSubmit={handleSearchSubmit}>
              <input
                type="text"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Buscar vacina por nome"
              />
              <button type="submit" disabled={isLoading}>
                {isLoading ? 'Buscando...' : 'Buscar'}
              </button>
              {activeSearch ? (
                <button
                  type="button"
                  onClick={handleClearSearch}
                  disabled={isLoading}
                >
                  Limpar
                </button>
              ) : null}
            </form>

            <p className="animals-count">
              {isLoading
                ? 'Carregando...'
                : activeSearch
                  ? `${vacinas.length} ${vacinas.length === 1 ? 'resultado' : 'resultados'} para "${activeSearch}"`
                  : `${vacinas.length} ${vacinas.length === 1 ? 'vacina cadastrada' : 'vacinas cadastradas'}`}
            </p>

            {feedback.message ? (
              <p
                className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
              >
                {feedback.message}
              </p>
            ) : null}

            {vacinas.length ? (
              <div className="animals-grid">
                {vacinas.map((vacina) => (
                  <VacinaCard
                    key={vacina.id}
                    vacina={vacina}
                    onEditar={openEditModal}
                  />
                ))}
              </div>
            ) : (
              <div className="animals-empty">
                {activeSearch ? (
                  <>
                    <p>Nenhuma vacina encontrada.</p>
                    <span>
                      Nenhum resultado para "{activeSearch}". Ajuste a busca.
                    </span>
                  </>
                ) : (
                  <>
                    <p>Nenhuma vacina cadastrada.</p>
                    <span>Clique no botão + para cadastrar a primeira vacina.</span>
                  </>
                )}
              </div>
            )}

            <button
              type="button"
              className="fab-add"
              aria-label="Adicionar vacina"
              onClick={openCreateModal}
            >
              +
            </button>
          </>
        ) : null}

        {activeTab === 'estoque' ? (
          <>
            {estoqueFeedback.message ? (
              <p
                className={`feedback ${estoqueFeedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
              >
                {estoqueFeedback.message}
              </p>
            ) : null}

            <div className="data-toolbar">
              <form className="toolbar-search" onSubmit={handleEstoqueSearchSubmit}>
                <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
                <input
                  type="text"
                  value={estoqueSearch}
                  onChange={(e) => setEstoqueSearch(e.target.value)}
                  placeholder="Buscar insumo por nome"
                />
                {estoqueActiveSearch ? (
                  <button
                    type="button"
                    className="toolbar-search__clear"
                    onClick={handleEstoqueClearSearch}
                    aria-label="Limpar busca"
                  >
                    ✕
                  </button>
                ) : null}
              </form>

              {canGerenciarEstoque ? (
                <button type="button" className="btn-new-entity" onClick={openCreateEstoqueModal}>
                  + Novo Insumo
                </button>
              ) : null}
            </div>

            <div className="data-table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>Tipo</th>
                    <th>Saldo</th>
                    <th>Estoque Mínimo</th>
                    <th>Preço Médio</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {isLoadingEstoque ? (
                    <tr>
                      <td colSpan={6} className="table-loading">Carregando...</td>
                    </tr>
                  ) : insumosEstoque.length === 0 ? (
                    <tr>
                      <td colSpan={6} className="table-empty">
                        {estoqueActiveSearch
                          ? `Nenhum resultado para "${estoqueActiveSearch}".`
                          : canGerenciarEstoque
                            ? 'Nenhum insumo cadastrado. Clique em "+ Novo Insumo" para começar.'
                            : 'Nenhum insumo cadastrado.'}
                      </td>
                    </tr>
                  ) : (
                    insumosEstoque.map((insumo) => (
                      <tr key={insumo.id}>
                        <td>
                          {insumo.nome}
                          {insumo.abaixoDoEstoqueMinimo ? (
                            <span className="vacina-badge vacina-badge--pendente estoque-badge">
                              Abaixo do mínimo
                            </span>
                          ) : null}
                        </td>
                        <td>{insumo.tipo}</td>
                        <td>
                          {insumo.saldoAtual} {insumo.unidadeMedidaPrimariaSigla}
                        </td>
                        <td>
                          {insumo.estoqueMinimo != null
                            ? `${insumo.estoqueMinimo} ${insumo.unidadeMedidaPrimariaSigla}`
                            : '—'}
                        </td>
                        <td>
                          {insumo.precoCompraMedio != null
                            ? `R$ ${insumo.precoCompraMedio.toFixed(2)}`
                            : '—'}
                        </td>
                        <td>
                          <div className="row-actions">
                            {canGerenciarEstoque ? (
                              <>
                                <button
                                  type="button"
                                  className="btn-row"
                                  onClick={() => openEntradaModal(insumo)}
                                >
                                  Registrar Entrada
                                </button>
                                <button
                                  type="button"
                                  className="btn-row btn-row--edit"
                                  onClick={() => openEditEstoqueModal(insumo)}
                                >
                                  Editar
                                </button>
                              </>
                            ) : (
                              <span>—</span>
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
        ) : null}

        {activeTab === 'alimentar' ? (
          <AlimentarLoteTab currentUser={currentUser} insumosEstoque={insumosEstoque} />
        ) : null}
      </section>

      {modal.open ? (
        <VacinaFormModal
          mode={formMode}
          formData={formData}
          isSaving={isSaving}
          isDeleting={isDeleting}
          feedback={formFeedback}
          onClose={closeModal}
          onChange={handleFormChange}
          onSubmit={handleSubmitForm}
          onConfirmar={handleConfirmar}
          onDelete={handleDelete}
        />
      ) : null}

      {estoqueModal.type === 'form' ? (
        <InsumoEstoqueFormModal
          mode={estoqueFormMode}
          formData={estoqueFormData}
          unidades={unidades}
          isSaving={isSavingEstoque}
          feedback={estoqueFormFeedback}
          onClose={closeEstoqueModal}
          onChange={handleEstoqueFormChange}
          onSubmit={handleSubmitEstoqueForm}
        />
      ) : null}

      {estoqueModal.type === 'entrada' && estoqueModal.insumo ? (
        <EntradaEstoqueModal
          insumo={estoqueModal.insumo}
          formData={entradaFormData}
          isSaving={isSavingEntrada}
          feedback={entradaFeedback}
          onClose={closeEstoqueModal}
          onChange={handleEntradaFormChange}
          onSubmit={handleSubmitEntrada}
        />
      ) : null}
    </main>
  )
}

export default InsumosPage
