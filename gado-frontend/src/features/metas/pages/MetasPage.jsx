import { useCallback, useEffect, useState } from 'react'
import MetaCard from '../components/MetaCard'
import MetaFormModal from '../components/MetaFormModal'
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
        <div className="animals-logo">🌿</div>
        <nav>
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('lotes')}>
            Lotes
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('setores')}>
            Setores
          </button>
          <button type="button" className="menu-item menu-item--active">
            Metas
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('insumos')}>
            Insumos
          </button>
          <button type="button" className="menu-item">
            Financeiro
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('perfil')}>
            Perfil
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
        <header className="animals-header">
          <h1>Metas de Setores</h1>
          <span>{currentUser.email}</span>
        </header>

        {feedback.message ? (
          <p className={`feedback feedback--${feedback.type === 'error' ? 'error' : 'info'}`}>{feedback.message}</p>
        ) : null}

        <div className="metas-toolbar">
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

          <button type="button" disabled={!setorSelecionado || metas.length === 0} onClick={() => exportarMetasCSV(metas)}>
            Exportar CSV
          </button>
          <button type="button" disabled={!setorSelecionado} onClick={() => exportarMetasPDF(setorSelecionado)}>
            Exportar PDF
          </button>

          {podeGerenciar ? (
            <button type="button" className="fab-add-inline" onClick={() => setModal({ type: 'create', meta: null })}>
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
            {podeGerenciar ? <span>Clique em "+ Nova Meta" para cadastrar a primeira meta.</span> : null}
          </div>
        ) : null}

        <p className="animals-count">
          {isLoading
            ? 'Carregando...'
            : setorSelecionado
              ? `${metas.length} ${metas.length === 1 ? 'meta cadastrada' : 'metas cadastradas'}`
              : 'Selecione um setor para ver as metas.'}
        </p>
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
