import { useCallback, useEffect, useRef, useState } from 'react'
import MetaCard from '../components/MetaCard'
import MetaFormModal from '../components/MetaFormModal'
import ModuleHeader from '../../../components/shared/ModuleHeader'
import { listarMetasPorSetor, deletarMeta, exportarMetasCSV, exportarMetasPDF } from '../integration/metaSetorApi'
import { listarSetores } from '../../setores/integration/setorApi'
import { listarLotes } from '../../lotes/integration/loteApi'
import '../../animais/styles/animais.css'
import '../styles/metas.css'

function MetasPage({ currentUser, onNavigate, onLogout }) {
  const [setores, setSetores] = useState([])
  const [lotes, setLotes] = useState([])
  const [setorSelecionado, setSetorSelecionado] = useState('')
  const [metas, setMetas] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [modal, setModal] = useState({ type: null, meta: null })
  const [exportMenuOpen, setExportMenuOpen] = useState(false)
  const exportMenuRef = useRef(null)

  const podeGerenciar = currentUser.perfil === 'ADMINISTRADOR' || currentUser.perfil === 'GERENTE'

  const lotesDoSetor = setorSelecionado
    ? lotes.filter(
        (l) => l.statusLote === 'ATIVO' && l.alocacoes.some((a) => String(a.setorId) === String(setorSelecionado)),
      )
    : lotes.filter((l) => l.statusLote === 'ATIVO')

  useEffect(() => {
    listarSetores().then(setSetores).catch(() => setSetores([]))
    listarLotes().then(setLotes).catch(() => setLotes([]))
  }, [])

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
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar metas.' })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchMetas(setorSelecionado)
  }, [setorSelecionado, fetchMetas])

  function closeModal() {
    setModal({ type: null, meta: null })
  }

  function handleMetaSalva() {
    closeModal()
    fetchMetas(setorSelecionado)
    setFeedback({ type: 'info', message: 'Meta salva com sucesso.' })
  }

  async function handleDeletar(meta) {
    if (!window.confirm(`Excluir a meta do setor "${meta.setorNome}"? Esta ação também remove o histórico de medições.`)) return

    setFeedback({ type: '', message: '' })
    try {
      await deletarMeta(meta.id, currentUser.email)
      setFeedback({ type: 'info', message: 'Meta excluída com sucesso.' })
      fetchMetas(setorSelecionado)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao excluir a meta.' })
    }
  }

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo"><img src="/logo.png" alt="GADO" /></div>
        <nav>
          <button type="button" className="menu-item" onClick={() => onNavigate('resumo')}>
            Resumo
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('lotes')}>
            Lotes
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('setores')}>
            Setores
          </button>
          <button type="button" className="menu-item menu-item--active" onClick={() => onNavigate('metas')}>
            Metas
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('insumos')}>
            Insumos
          </button>
          {!['CUIDADOR', 'CUIDADOR_CHEFE'].includes(currentUser?.perfil) ? (
            <button type="button" className="menu-item" onClick={() => onNavigate('financeiro')}>
              Financeiro
            </button>
          ) : null}
          <button type="button" className="menu-item" onClick={() => onNavigate('perfil')}>
            Perfil
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('tarefas')}>
            Tarefas
          </button>
          {currentUser.perfil === 'ADMINISTRADOR' && (
            <button type="button" className="menu-item" onClick={() => onNavigate('configuracoes')}>
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

      <section className="animals-content">
        <ModuleHeader
          icon="target"
          title="Metas dos setores"
          description="Transforme o planejamento em números simples e acompanhe a evolução de cada setor."
          metrics={[
            { value: setores.length, label: 'Setores' },
            { value: metas.length, label: 'Metas exibidas' },
            { value: lotesDoSetor.length, label: 'Lotes ativos' },
          ]}
        />

        {feedback.message ? (
          <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>{feedback.message}</p>
        ) : null}

        <div className="data-toolbar">
          <div className="metas-setor-filter">
            <label htmlFor="filtro-setor">Setor:</label>
            <select id="filtro-setor" value={setorSelecionado} onChange={(e) => setSetorSelecionado(e.target.value)}>
              <option value="">Selecione um setor...</option>
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
              className="btn-export-csv"
              disabled={!setorSelecionado || metas.length === 0}
              onClick={() => setExportMenuOpen((v) => !v)}
            >
              Exportar ▾
            </button>
            {exportMenuOpen ? (
              <div className="export-menu">
                <button type="button" className="export-menu__item" onClick={handleExportarCSV}>
                  Exportar como CSV
                </button>
                <hr className="export-menu__separator" />
                <button type="button" className="export-menu__item" onClick={handleExportarPDF}>
                  Exportar como PDF
                </button>
              </div>
            ) : null}
          </div>

          {podeGerenciar ? (
            <button type="button" className="btn-new-entity" onClick={() => setModal({ type: 'create', meta: null })}>
              + Nova Meta
            </button>
          ) : null}
        </div>

        {!isLoading && metas.length > 0 ? (
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
        ) : null}

        {!isLoading && setorSelecionado && metas.length === 0 && !feedback.message ? (
          <div className="metas-estado-vazio">
            <p>Nenhuma meta cadastrada para este setor.</p>
            {podeGerenciar ? <span>Clique em &quot;+ Nova Meta&quot; para cadastrar a primeira meta.</span> : null}
          </div>
        ) : null}

        <footer className="data-pagination">
          <span className="pagination-info">
            {isLoading
              ? 'Carregando...'
              : setorSelecionado
                ? `${metas.length} ${metas.length === 1 ? 'meta cadastrada' : 'metas cadastradas'}`
                : 'Selecione um setor para ver as metas.'}
          </span>
        </footer>
      </section>

      {modal.type === 'create' || modal.type === 'edit' ? (
        <MetaFormModal
          mode={modal.type}
          meta={modal.meta}
          setores={setores}
          emailUsuario={currentUser.email}
          onClose={closeModal}
          onSaved={handleMetaSalva}
        />
      ) : null}
    </main>
  )
}

export default MetasPage
