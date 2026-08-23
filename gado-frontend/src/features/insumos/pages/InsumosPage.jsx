import { useCallback, useEffect, useState } from 'react'
import GrupoProdutoFormModal from '../components/GrupoProdutoFormModal'
import UnidadeMedidaFormModal from '../components/UnidadeMedidaFormModal'
import InsumoEstoqueFormModal from '../components/InsumoEstoqueFormModal'
import EntradaEstoqueModal from '../components/EntradaEstoqueModal'
import AlimentarSetoresTab from '../components/AlimentarSetoresTab'
import ConsumoEstoqueTab from '../components/ConsumoEstoqueTab'
import VacinarAnimaisTab from '../components/VacinarAnimaisTab'
import {
  listarEstoque,
  cadastrarInsumoEstoque,
  atualizarInsumoEstoque,
  registrarEntradaEstoque,
  inativarInsumoEstoque,
  reativarInsumoEstoque,
} from '../integration/insumoApi'
import {
  listarUnidadesMedida,
  cadastrarUnidadeMedida,
  atualizarUnidadeMedida,
  deletarUnidadeMedida,
  reativarUnidadeMedida,
} from '../integration/unidadeMedidaApi'
import {
  atualizarGrupoProduto,
  cadastrarGrupoProduto,
  deletarGrupoProduto,
  reativarGrupoProduto,
  listarGruposProduto,
} from '../integration/grupoProdutoApi'
import '../../animais/styles/animais.css'
import '../styles/insumos.css'

const PERFIS_GESTAO_ESTOQUE = ['ADMINISTRADOR', 'GERENTE', 'CUIDADOR_CHEFE']

const defaultGrupoForm = {
  id: null,
  nome: '',
  codigoPrefixo: '',
  naturezaFinanceira: '',
}

const defaultUnidadeForm = {
  id: null,
  unidade: '',
}

const defaultEstoqueForm = {
  nome: '',
  tipo: 'RACAO',
  grupoProdutoId: '',
  grupoProdutoNome: '',
  codigoProduto: '',
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
  const [activeTab, setActiveTab] = useState('estoque')
  const canGerenciarEstoque = PERFIS_GESTAO_ESTOQUE.includes(currentUser?.perfil)

  // ── Grupos de Produto ────────────────────────────────────────────────

  const [isLoadingGrupos, setIsLoadingGrupos] = useState(false)
  const [gruposFeedback, setGruposFeedback] = useState({ type: '', message: '' })
  const [grupos, setGrupos] = useState([])
  const [grupoModal, setGrupoModal] = useState({ open: false, grupo: null })
  const [grupoFormMode, setGrupoFormMode] = useState('create')
  const [grupoFormData, setGrupoFormData] = useState(defaultGrupoForm)
  const [grupoFormFeedback, setGrupoFormFeedback] = useState('')
  const [isSavingGrupo, setIsSavingGrupo] = useState(false)
  const [grupoSearch, setGrupoSearch] = useState('')
  const [grupoActiveSearch, setGrupoActiveSearch] = useState('')
  const [grupoStatusFiltro, setGrupoStatusFiltro] = useState('ATIVO')

  const fetchGrupos = useCallback(async (termo, statusFiltro) => {
    setIsLoadingGrupos(true)
    setGruposFeedback({ type: '', message: '' })
    try {
      const list = await listarGruposProduto(termo ?? '', statusFiltro ?? 'ATIVO')
      setGrupos(list)
    } catch (error) {
      setGruposFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar os grupos de produto.',
      })
    } finally {
      setIsLoadingGrupos(false)
    }
  }, [])

  function handleGrupoSearchSubmit(event) {
    event.preventDefault()
    const termo = grupoSearch.trim()
    setGrupoActiveSearch(termo)
    fetchGrupos(termo, grupoStatusFiltro)
  }

  function handleGrupoClearSearch() {
    setGrupoSearch('')
    setGrupoActiveSearch('')
    fetchGrupos('', grupoStatusFiltro)
  }

  function handleGrupoStatusFiltroChange(event) {
    const novoStatus = event.target.value
    setGrupoStatusFiltro(novoStatus)
    fetchGrupos(grupoActiveSearch, novoStatus)
  }

  function closeGrupoModal() {
    setGrupoModal({ open: false, grupo: null })
    setGrupoFormData(defaultGrupoForm)
    setGrupoFormFeedback('')
  }

  function handleGrupoFormChange(event) {
    const { name, value } = event.target
    setGrupoFormData((current) => ({ ...current, [name]: value }))
  }

  function openCreateGrupoModal() {
    setGrupoFormMode('create')
    setGrupoFormData(defaultGrupoForm)
    setGrupoFormFeedback('')
    setGrupoModal({ open: true, grupo: null })
  }

  function openEditGrupoModal(grupo) {
    setGrupoFormMode('edit')
    setGrupoFormFeedback('')
    setGrupoFormData({
      id: grupo.id,
      nome: grupo.nome,
      codigoPrefixo: grupo.codigoPrefixo,
      naturezaFinanceira: grupo.naturezaFinanceira,
    })
    setGrupoModal({ open: true, grupo })
  }

  async function handleSubmitGrupoForm(event) {
    event.preventDefault()
    setIsSavingGrupo(true)
    setGrupoFormFeedback('')
    setGruposFeedback({ type: '', message: '' })
    try {
      if (grupoFormMode === 'create') {
        await cadastrarGrupoProduto(grupoFormData)
        setGruposFeedback({ type: 'info', message: 'Grupo cadastrado com sucesso.' })
      } else {
        await atualizarGrupoProduto(grupoFormData.id, grupoFormData)
        setGruposFeedback({ type: 'info', message: 'Grupo atualizado com sucesso.' })
      }
      closeGrupoModal()
      await fetchGrupos()
    } catch (error) {
      setGrupoFormFeedback(error.message || 'Falha ao salvar o grupo.')
    } finally {
      setIsSavingGrupo(false)
    }
  }

  async function handleDeletarGrupo(grupo) {
    const confirmar = window.confirm(`Deseja inativar o grupo "${grupo.nome}"?`)
    if (!confirmar) return

    setGruposFeedback({ type: '', message: '' })
    try {
      await deletarGrupoProduto(grupo.id)
      setGruposFeedback({ type: 'info', message: 'Grupo inativado com sucesso.' })
      await fetchGrupos(grupoActiveSearch, grupoStatusFiltro)
    } catch (error) {
      setGruposFeedback({
        type: 'error',
        message: error.message || 'Falha ao inativar o grupo.',
      })
    }
  }

  async function handleReativarGrupo(grupo) {
    setGruposFeedback({ type: '', message: '' })
    try {
      await reativarGrupoProduto(grupo.id)
      setGruposFeedback({ type: 'info', message: 'Grupo reativado com sucesso.' })
      await fetchGrupos(grupoActiveSearch, grupoStatusFiltro)
    } catch (error) {
      setGruposFeedback({
        type: 'error',
        message: error.message || 'Falha ao reativar o grupo.',
      })
    }
  }

  // ── Unidades de Medida ───────────────────────────────────────────────

  const [isLoadingUnidadesGerenciadas, setIsLoadingUnidadesGerenciadas] = useState(false)
  const [unidadesFeedback, setUnidadesFeedback] = useState({ type: '', message: '' })
  const [unidadesGerenciadas, setUnidadesGerenciadas] = useState([])
  const [unidadeModal, setUnidadeModal] = useState({ open: false, unidade: null })
  const [unidadeFormMode, setUnidadeFormMode] = useState('create')
  const [unidadeFormData, setUnidadeFormData] = useState(defaultUnidadeForm)
  const [unidadeFormFeedback, setUnidadeFormFeedback] = useState('')
  const [isSavingUnidade, setIsSavingUnidade] = useState(false)
  const [unidadeSearch, setUnidadeSearch] = useState('')
  const [unidadeActiveSearch, setUnidadeActiveSearch] = useState('')
  const [unidadeStatusFiltro, setUnidadeStatusFiltro] = useState('ATIVO')

  const fetchUnidadesGerenciadas = useCallback(async (termo, statusFiltro) => {
    setIsLoadingUnidadesGerenciadas(true)
    setUnidadesFeedback({ type: '', message: '' })
    try {
      const list = await listarUnidadesMedida(termo ?? '', statusFiltro ?? 'ATIVO')
      setUnidadesGerenciadas(list)
    } catch (error) {
      setUnidadesFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar as unidades de medida.',
      })
    } finally {
      setIsLoadingUnidadesGerenciadas(false)
    }
  }, [])

  function handleUnidadeSearchSubmit(event) {
    event.preventDefault()
    const termo = unidadeSearch.trim()
    setUnidadeActiveSearch(termo)
    fetchUnidadesGerenciadas(termo, unidadeStatusFiltro)
  }

  function handleUnidadeClearSearch() {
    setUnidadeSearch('')
    setUnidadeActiveSearch('')
    fetchUnidadesGerenciadas('', unidadeStatusFiltro)
  }

  function handleUnidadeStatusFiltroChange(event) {
    const novoStatus = event.target.value
    setUnidadeStatusFiltro(novoStatus)
    fetchUnidadesGerenciadas(unidadeActiveSearch, novoStatus)
  }

  function closeUnidadeModal() {
    setUnidadeModal({ open: false, unidade: null })
    setUnidadeFormData(defaultUnidadeForm)
    setUnidadeFormFeedback('')
  }

  function handleUnidadeFormChange(event) {
    const { name, value } = event.target
    setUnidadeFormData((current) => ({ ...current, [name]: value }))
  }

  function openCreateUnidadeModal() {
    setUnidadeFormMode('create')
    setUnidadeFormData(defaultUnidadeForm)
    setUnidadeFormFeedback('')
    setUnidadeModal({ open: true, unidade: null })
  }

  function openEditUnidadeModal(unidade) {
    setUnidadeFormMode('edit')
    setUnidadeFormFeedback('')
    setUnidadeFormData({
      id: unidade.id,
      unidade: unidade.unidade,
    })
    setUnidadeModal({ open: true, unidade })
  }

  async function handleSubmitUnidadeForm(event) {
    event.preventDefault()
    setIsSavingUnidade(true)
    setUnidadeFormFeedback('')
    setUnidadesFeedback({ type: '', message: '' })
    try {
      if (unidadeFormMode === 'create') {
        await cadastrarUnidadeMedida(unidadeFormData)
        setUnidadesFeedback({ type: 'info', message: 'Unidade de medida cadastrada com sucesso.' })
      } else {
        await atualizarUnidadeMedida(unidadeFormData.id, unidadeFormData)
        setUnidadesFeedback({ type: 'info', message: 'Unidade de medida atualizada com sucesso.' })
      }
      closeUnidadeModal()
      await fetchUnidadesGerenciadas(unidadeActiveSearch, unidadeStatusFiltro)
      await fetchUnidades()
    } catch (error) {
      setUnidadeFormFeedback(error.message || 'Falha ao salvar a unidade de medida.')
    } finally {
      setIsSavingUnidade(false)
    }
  }

  async function handleDeletarUnidade(unidade) {
    const confirmar = window.confirm(`Deseja inativar a unidade "${unidade.unidade}"?`)
    if (!confirmar) return

    setUnidadesFeedback({ type: '', message: '' })
    try {
      await deletarUnidadeMedida(unidade.id)
      setUnidadesFeedback({ type: 'info', message: 'Unidade de medida inativada com sucesso.' })
      await fetchUnidadesGerenciadas(unidadeActiveSearch, unidadeStatusFiltro)
      await fetchUnidades()
    } catch (error) {
      setUnidadesFeedback({
        type: 'error',
        message: error.message || 'Falha ao inativar a unidade de medida.',
      })
    }
  }

  async function handleReativarUnidade(unidade) {
    setUnidadesFeedback({ type: '', message: '' })
    try {
      await reativarUnidadeMedida(unidade.id)
      setUnidadesFeedback({ type: 'info', message: 'Unidade de medida reativada com sucesso.' })
      await fetchUnidadesGerenciadas(unidadeActiveSearch, unidadeStatusFiltro)
      await fetchUnidades()
    } catch (error) {
      setUnidadesFeedback({
        type: 'error',
        message: error.message || 'Falha ao reativar a unidade de medida.',
      })
    }
  }

  // ── Estoque (Catálogo geral de Insumos) ──────────────────────────────

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
  const [estoqueStatusFiltro, setEstoqueStatusFiltro] = useState('ATIVO')

  const fetchEstoque = useCallback(async (termo, statusFiltro) => {
    setIsLoadingEstoque(true)
    setEstoqueFeedback({ type: '', message: '' })
    try {
      const list = await listarEstoque(termo, statusFiltro ?? 'ATIVO')
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

  const fetchUnidades = useCallback(async () => {
    try {
      const list = await listarUnidadesMedida()
      setUnidades(list)
    } catch {
      setUnidades([])
    }
  }, [])

  useEffect(() => {
    fetchEstoque('', 'ATIVO')
    fetchGrupos('', 'ATIVO')
    fetchUnidadesGerenciadas('', 'ATIVO')
    fetchUnidades()
  }, [fetchEstoque, fetchGrupos, fetchUnidadesGerenciadas, fetchUnidades])

  function handleEstoqueSearchSubmit(event) {
    event.preventDefault()
    const termo = estoqueSearch.trim()
    setEstoqueActiveSearch(termo)
    fetchEstoque(termo, estoqueStatusFiltro)
  }

  function handleEstoqueClearSearch() {
    setEstoqueSearch('')
    setEstoqueActiveSearch('')
    fetchEstoque('', estoqueStatusFiltro)
  }

  function handleEstoqueStatusFiltroChange(event) {
    const novoStatus = event.target.value
    setEstoqueStatusFiltro(novoStatus)
    fetchEstoque(estoqueActiveSearch, novoStatus)
  }

  async function handleInativarProduto(insumo) {
    const confirmar = window.confirm(`Deseja inativar o produto "${insumo.nome}"?`)
    if (!confirmar) return
    setEstoqueFeedback({ type: '', message: '' })
    try {
      await inativarInsumoEstoque(insumo.id, currentUser.email)
      setEstoqueFeedback({ type: 'info', message: 'Produto inativado com sucesso.' })
      await fetchEstoque(estoqueActiveSearch, estoqueStatusFiltro)
    } catch (error) {
      setEstoqueFeedback({ type: 'error', message: error.message || 'Falha ao inativar o produto.' })
    }
  }

  async function handleReativarProduto(insumo) {
    setEstoqueFeedback({ type: '', message: '' })
    try {
      await reativarInsumoEstoque(insumo.id, currentUser.email)
      setEstoqueFeedback({ type: 'info', message: 'Produto reativado com sucesso.' })
      await fetchEstoque(estoqueActiveSearch, estoqueStatusFiltro)
    } catch (error) {
      setEstoqueFeedback({ type: 'error', message: error.message || 'Falha ao reativar o produto.' })
    }
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
      grupoProdutoId: insumo.grupoProdutoId ?? '',
      grupoProdutoNome: insumo.grupoProdutoNome,
      codigoProduto: insumo.codigoProduto,
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
        setEstoqueFeedback({ type: 'info', message: 'Produto cadastrado com sucesso.' })
      } else {
        await atualizarInsumoEstoque(estoqueModal.insumo.id, currentUser.email, estoqueFormData)
        setEstoqueFeedback({ type: 'info', message: 'Produto atualizado com sucesso.' })
      }
      closeEstoqueModal()
      await fetchEstoque(estoqueActiveSearch, estoqueStatusFiltro)
    } catch (error) {
      setEstoqueFormFeedback(error.message || 'Falha ao salvar o produto.')
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
      await fetchEstoque(estoqueActiveSearch, estoqueStatusFiltro)
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
          {!['CUIDADOR', 'CUIDADOR_CHEFE'].includes(currentUser?.perfil) ? (
            <button type="button" className="menu-item" onClick={() => onNavigate('financeiro')}>
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
            className={`insumos-tab ${activeTab === 'estoque' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('estoque')}
          >
            Catálogo de Produtos
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'grupos' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('grupos')}
          >
            Grupos de Produto
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'unidades' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('unidades')}
          >
            Unidades de Medida
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'alimentar' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('alimentar')}
          >
            Alimentar Setores
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'consumo' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('consumo')}
          >
            Consumo de Estoque
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'vacinar' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('vacinar')}
          >
            Vacinar Animais
          </button>
        </div>

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
                  placeholder="Buscar produto por nome"
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

              <select
                className="toolbar-select"
                value={estoqueStatusFiltro}
                onChange={handleEstoqueStatusFiltroChange}
              >
                <option value="ATIVO">Ativos</option>
                <option value="INATIVO">Inativos</option>
                <option value="TODOS">Todos</option>
              </select>

              {canGerenciarEstoque ? (
                <button type="button" className="btn-new-entity" onClick={openCreateEstoqueModal}>
                  + Novo Produto
                </button>
              ) : null}
            </div>

            <div className="data-table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Código</th>
                    <th>Nome</th>
                    <th>Grupo</th>
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
                      <td colSpan={8} className="table-loading">Carregando...</td>
                    </tr>
                  ) : insumosEstoque.length === 0 ? (
                    <tr>
                      <td colSpan={8} className="table-empty">
                        {estoqueActiveSearch
                          ? `Nenhum resultado para "${estoqueActiveSearch}".`
                          : canGerenciarEstoque
                            ? 'Nenhum produto cadastrado. Clique em "+ Novo Produto" para começar.'
                            : 'Nenhum produto cadastrado.'}
                      </td>
                    </tr>
                  ) : (
                    insumosEstoque.map((insumo) => (
                      <tr key={insumo.id}>
                        <td className="codigo-produto">{insumo.codigoProduto || '—'}</td>
                        <td>
                          {insumo.nome}
                          {insumo.status === 'INATIVO' ? (
                            <span className="setor-badge setor-badge--inativo estoque-badge">Inativo</span>
                          ) : null}
                          {insumo.abaixoDoEstoqueMinimo ? (
                            <span className="vacina-badge vacina-badge--pendente estoque-badge">
                              Abaixo do mínimo
                            </span>
                          ) : null}
                        </td>
                        <td>{insumo.grupoProdutoNome || '—'}</td>
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
                              insumo.status === 'INATIVO' ? (
                                <button
                                  type="button"
                                  className="btn-row"
                                  onClick={() => handleReativarProduto(insumo)}
                                >
                                  Reativar
                                </button>
                              ) : (
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
                                  <button
                                    type="button"
                                    className="btn-row btn-row--danger"
                                    onClick={() => handleInativarProduto(insumo)}
                                  >
                                    Inativar
                                  </button>
                                </>
                              )
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

        {activeTab === 'grupos' ? (
          <>
            {gruposFeedback.message ? (
              <p
                className={`feedback ${gruposFeedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
              >
                {gruposFeedback.message}
              </p>
            ) : null}

            <div className="data-toolbar">
              <form className="toolbar-search" onSubmit={handleGrupoSearchSubmit}>
                <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
                <input
                  type="text"
                  value={grupoSearch}
                  onChange={(e) => setGrupoSearch(e.target.value)}
                  placeholder="Buscar grupo por nome"
                />
                {grupoActiveSearch ? (
                  <button
                    type="button"
                    className="toolbar-search__clear"
                    onClick={handleGrupoClearSearch}
                    aria-label="Limpar busca"
                  >
                    ✕
                  </button>
                ) : null}
              </form>

              <select
                className="toolbar-select"
                value={grupoStatusFiltro}
                onChange={handleGrupoStatusFiltroChange}
              >
                <option value="ATIVO">Ativos</option>
                <option value="INATIVO">Inativos</option>
                <option value="TODOS">Todos</option>
              </select>

              <p className="animals-count">
                {isLoadingGrupos
                  ? 'Carregando...'
                  : `${grupos.length} ${grupos.length === 1 ? 'grupo cadastrado' : 'grupos cadastrados'}`}
              </p>

              {canGerenciarEstoque ? (
                <button type="button" className="btn-new-entity" onClick={openCreateGrupoModal}>
                  + Novo Grupo
                </button>
              ) : null}
            </div>

            <div className="data-table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Prefixo</th>
                    <th>Nome</th>
                    <th>Natureza</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {isLoadingGrupos ? (
                    <tr>
                      <td colSpan={4} className="table-loading">Carregando...</td>
                    </tr>
                  ) : grupos.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="table-empty">
                        {canGerenciarEstoque
                          ? 'Nenhum grupo cadastrado. Clique em "+ Novo Grupo" para começar.'
                          : 'Nenhum grupo cadastrado.'}
                      </td>
                    </tr>
                  ) : (
                    grupos.map((grupo) => (
                      <tr key={grupo.id}>
                        <td className="codigo-produto">{grupo.codigoPrefixo}</td>
                        <td>
                          {grupo.nome}
                          {grupo.status === 'INATIVO' ? (
                            <span className="setor-badge setor-badge--inativo estoque-badge">Inativo</span>
                          ) : null}
                        </td>
                        <td>{grupo.naturezaFinanceira === 'CUSTO' ? 'Custo' : 'Gasto'}</td>
                        <td>
                          <div className="row-actions">
                            {canGerenciarEstoque ? (
                              grupo.status === 'INATIVO' ? (
                                <button
                                  type="button"
                                  className="btn-row"
                                  onClick={() => handleReativarGrupo(grupo)}
                                >
                                  Reativar
                                </button>
                              ) : (
                                <>
                                  <button
                                    type="button"
                                    className="btn-row btn-row--edit"
                                    onClick={() => openEditGrupoModal(grupo)}
                                  >
                                    Editar
                                  </button>
                                  <button
                                    type="button"
                                    className="btn-row btn-row--danger"
                                    onClick={() => handleDeletarGrupo(grupo)}
                                  >
                                    Inativar
                                  </button>
                                </>
                              )
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

        {activeTab === 'unidades' ? (
          <>
            {unidadesFeedback.message ? (
              <p
                className={`feedback ${unidadesFeedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}
              >
                {unidadesFeedback.message}
              </p>
            ) : null}

            <div className="data-toolbar">
              <form className="toolbar-search" onSubmit={handleUnidadeSearchSubmit}>
                <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
                <input
                  type="text"
                  value={unidadeSearch}
                  onChange={(e) => setUnidadeSearch(e.target.value)}
                  placeholder="Buscar unidade"
                />
                {unidadeActiveSearch ? (
                  <button
                    type="button"
                    className="toolbar-search__clear"
                    onClick={handleUnidadeClearSearch}
                    aria-label="Limpar busca"
                  >
                    ✕
                  </button>
                ) : null}
              </form>

              <select
                className="toolbar-select"
                value={unidadeStatusFiltro}
                onChange={handleUnidadeStatusFiltroChange}
              >
                <option value="ATIVO">Ativas</option>
                <option value="INATIVO">Inativas</option>
                <option value="TODOS">Todas</option>
              </select>

              <p className="animals-count">
                {isLoadingUnidadesGerenciadas
                  ? 'Carregando...'
                  : `${unidadesGerenciadas.length} ${unidadesGerenciadas.length === 1 ? 'unidade cadastrada' : 'unidades cadastradas'}`}
              </p>

              {canGerenciarEstoque ? (
                <button type="button" className="btn-new-entity" onClick={openCreateUnidadeModal}>
                  + Nova Unidade
                </button>
              ) : null}
            </div>

            <div className="data-table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Unidade</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {isLoadingUnidadesGerenciadas ? (
                    <tr>
                      <td colSpan={2} className="table-loading">Carregando...</td>
                    </tr>
                  ) : unidadesGerenciadas.length === 0 ? (
                    <tr>
                      <td colSpan={2} className="table-empty">
                        {canGerenciarEstoque
                          ? 'Nenhuma unidade cadastrada. Clique em "+ Nova Unidade" para começar.'
                          : 'Nenhuma unidade cadastrada.'}
                      </td>
                    </tr>
                  ) : (
                    unidadesGerenciadas.map((unidade) => (
                      <tr key={unidade.id}>
                        <td>
                          {unidade.unidade}
                          {unidade.status === 'INATIVO' ? (
                            <span className="setor-badge setor-badge--inativo estoque-badge">Inativo</span>
                          ) : null}
                        </td>
                        <td>
                          <div className="row-actions">
                            {canGerenciarEstoque ? (
                              unidade.status === 'INATIVO' ? (
                                <button
                                  type="button"
                                  className="btn-row"
                                  onClick={() => handleReativarUnidade(unidade)}
                                >
                                  Reativar
                                </button>
                              ) : (
                                <>
                                  <button
                                    type="button"
                                    className="btn-row btn-row--edit"
                                    onClick={() => openEditUnidadeModal(unidade)}
                                  >
                                    Editar
                                  </button>
                                  <button
                                    type="button"
                                    className="btn-row btn-row--danger"
                                    onClick={() => handleDeletarUnidade(unidade)}
                                  >
                                    Inativar
                                  </button>
                                </>
                              )
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
          <AlimentarSetoresTab currentUser={currentUser} insumosEstoque={insumosEstoque} />
        ) : null}

        {activeTab === 'consumo' ? (
          <ConsumoEstoqueTab currentUser={currentUser} insumosEstoque={insumosEstoque} />
        ) : null}

        {activeTab === 'vacinar' ? (
          <VacinarAnimaisTab currentUser={currentUser} insumosEstoque={insumosEstoque} />
        ) : null}
      </section>

      {estoqueModal.type === 'form' ? (
        <InsumoEstoqueFormModal
          mode={estoqueFormMode}
          formData={estoqueFormData}
          unidades={unidades}
          grupos={grupos}
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

      {grupoModal.open ? (
        <GrupoProdutoFormModal
          mode={grupoFormMode}
          formData={grupoFormData}
          isSaving={isSavingGrupo}
          feedback={grupoFormFeedback}
          onClose={closeGrupoModal}
          onChange={handleGrupoFormChange}
          onSubmit={handleSubmitGrupoForm}
        />
      ) : null}

      {unidadeModal.open ? (
        <UnidadeMedidaFormModal
          mode={unidadeFormMode}
          formData={unidadeFormData}
          isSaving={isSavingUnidade}
          feedback={unidadeFormFeedback}
          onClose={closeUnidadeModal}
          onChange={handleUnidadeFormChange}
          onSubmit={handleSubmitUnidadeForm}
        />
      ) : null}
    </main>
  )
}

export default InsumosPage
