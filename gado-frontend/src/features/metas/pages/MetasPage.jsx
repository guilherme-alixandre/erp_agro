import { useCallback, useEffect, useRef, useState } from 'react'
import MetaCard from '../components/MetaCard'
import MetaFormModal from '../components/MetaFormModal'
import {
  listarMetasPorSetor,
  deletarMeta,
  exportarMetasCSV,
  exportarMetasPDF,
} from '../../../services/metaSetorApi'
import '../../animais/styles/animais.css'
import '../styles/metas.css'

/**
 * MetasPage — tela de listagem e gerenciamento de Metas de Setores.
 *
 * Props:
 *  - currentUser → objeto do usuário logado
 *  - setores     → lista [{ id, nome }] pré-carregada pelo App
 *  - lotes       → lista [{ id, descricao, setorId }] pré-carregada pelo App
 *  - onNavigate  → fn(page) — navegação entre módulos
 *  - onLogout    → fn() — encerra a sessão
 */
function MetasPage({ currentUser, setores, lotes, onNavigate, onLogout }) {
  const [setorSelecionado, setSetorSelecionado] = useState('')
  const [metas, setMetas] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [modal, setModal] = useState({ type: null, meta: null })
  const [isDeleting, setIsDeleting] = useState(false)
  const [exportMenuOpen, setExportMenuOpen] = useState(false)
  const exportMenuRef = useRef(null)

  // ── Permissões ───────────────────────────────────────────────────────
  const podeGerenciar =
    currentUser.perfil === 'ADMINISTRADOR' || currentUser.perfil === 'GERENTE'

  // ── Lotes do setor selecionado ────────────────────────────────────────
  const lotesDoSetor = setorSelecionado
    ? lotes.filter(
        (l) =>
          l.statusLote === 'ATIVO' &&
          l.alocacoes.some((a) => String(a.setorId) === String(setorSelecionado))
      )
    : lotes.filter((l) => l.statusLote === 'ATIVO')

  useEffect(() => {
    if (!exportMenuOpen) return
    function handleClickOutside(event) {
      if (exportMenuRef.current && !exportMenuRef.current.contains(event.target)) {
        setExportMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [exportMenuOpen])

  function handleExportarCSV() {
    exportarMetasCSV(metas)
    setExportMenuOpen(false)
  }

  function handleExportarPDF() {
    exportarMetasPDF(setorSelecionado)
    setExportMenuOpen(false)
  }

  // ── Carregar metas ────────────────────────────────────────────────────
  const fetchMetas = useCallback(async (setorId) => {
    if (!setorId) {
      setMetas([])
      return
    }
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const lista = await listarMetasPorSetor(setorId)
      setMetas(lista)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao carregar metas.',
      })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchMetas(setorSelecionado)
  }, [setorSelecionado, fetchMetas])

  // ── Ações do modal ────────────────────────────────────────────────────
  function closeModal() {
    setModal({ type: null, meta: null })
  }

  function handleMetaSalva() {
    closeModal()
    fetchMetas(setorSelecionado)
    setFeedback({ type: 'info', message: 'Meta salva com sucesso.' })
  }

  async function handleDeletar(meta) {
    if (!window.confirm(`Excluir a meta do setor "${meta.setorNome}"? Esta ação também removerá todas as medições.`)) return

    setIsDeleting(true)
    setFeedback({ type: '', message: '' })
    try {
      await deletarMeta(meta.id, currentUser.email)
      setFeedback({ type: 'info', message: 'Meta excluída com sucesso.' })
      fetchMetas(setorSelecionado)
    } catch (error) {
      setFeedback({
        type: 'error',
        message: error.message || 'Falha ao excluir a meta.',
      })
    } finally {
      setIsDeleting(false)
    }
  }

  // ── Render ────────────────────────────────────────────────────────────
  return (
    <main className="animals-layout">

      {/* Sidebar idêntica ao padrão do projeto */}
      <aside className="animals-sidebar">
        <div className="animals-logo">🌿</div>
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
              className="menu-item menu-item--active"
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

      {/* Conteúdo principal */}
      <section className="animals-content">
        <header className="animals-header">
          <h1>Metas de Setores</h1>
          <span>{currentUser.email}</span>
        </header>

        {/* Filtro por setor + botão exportar */}
        <div className="search-toolbar">
          <div className="metas-filter" style={{ flex: 1 }}>
            <label htmlFor="filtro-setor">Setor:</label>
            <select
              id="filtro-setor"
              value={setorSelecionado}
              onChange={(e) => setSetorSelecionado(e.target.value)}
            >
              <option value="">Selecione um setor para ver as metas...</option>
              {setores.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nome}
                </option>
              ))}
            </select>
          </div>

          <div className="export-wrapper" ref={exportMenuRef}>
            <button
              type="button"
              className="export-btn"
              disabled={!setorSelecionado || metas.length === 0}
              onClick={() => setExportMenuOpen((v) => !v)}
            >
              Exportar ▾
            </button>
            {exportMenuOpen && (
              <div className="export-menu">
                <button
                  type="button"
                  className="export-menu__item"
                  onClick={handleExportarCSV}
                >
                  Exportar como CSV
                </button>
                <hr className="export-menu__separator" />
                <button
                  type="button"
                  className="export-menu__item"
                  onClick={handleExportarPDF}
                >
                  Exportar como PDF
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Contagem e estado */}
        <p className="animals-count">
          {isLoading
            ? 'Carregando...'
            : !setorSelecionado
            ? 'Selecione um setor acima.'
            : `${metas.length} ${metas.length === 1 ? 'meta cadastrada' : 'metas cadastradas'}`}
        </p>

        {/* Feedback global */}
        {feedback.message && (
          <p
            className={`feedback ${
              feedback.type === 'error' ? 'feedback--error' : 'feedback--info'
            }`}
          >
            {feedback.message}
          </p>
        )}

        {/* Lista de cards */}
        {!isLoading && metas.length > 0 && (
          <div className="metas-list">
            {metas.map((meta) => (
              <MetaCard
                key={meta.id}
                meta={meta}
                lotes={lotesDoSetor}
                currentUser={currentUser}
                podeGerenciar={podeGerenciar}
                onEditar={(m) => setModal({ type: 'edit', meta: m })}
                onDeletar={handleDeletar}
                onRefresh={() => fetchMetas(setorSelecionado)}
              />
            ))}
          </div>
        )}

        {/* Estado vazio */}
        {!isLoading && setorSelecionado && metas.length === 0 && !feedback.message && (
          <div className="animals-empty">
            <p>Nenhuma meta cadastrada.</p>
            <span>Clique no botão + para cadastrar a primeira meta deste setor.</span>
          </div>
        )}

        {/* FAB — só para quem pode gerenciar */}
        {podeGerenciar && (
          <button
            type="button"
            className="fab-add"
            aria-label="Adicionar meta"
            onClick={() => setModal({ type: 'create', meta: null })}
          >
            +
          </button>
        )}
      </section>

      {/* Modal de cadastro/edição */}
      {(modal.type === 'create' || modal.type === 'edit') && (
        <MetaFormModal
          mode={modal.type}
          meta={modal.meta}
          setores={setores}
          emailUsuario={currentUser.email}
          onClose={closeModal}
          onSaved={handleMetaSalva}
        />
      )}
    </main>
  )
}

export default MetasPage
