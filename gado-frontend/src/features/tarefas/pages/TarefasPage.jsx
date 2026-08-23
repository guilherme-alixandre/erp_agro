import { useCallback, useEffect, useState } from 'react'
import { listarMinhasTarefas, atribuirTarefa, concluirTarefa, excluirTarefa } from '../integration/tarefaApi'
import { listarUsuariosResumo } from '../integration/usuarioResumoApi'
import AtribuirTarefaModal from '../components/AtribuirTarefaModal'
import '../../animais/styles/animais.css'

const defaultForm = { descricao: '', dataLimite: '', atribuidoParaEmail: '' }

function formatDate(dateText) {
  if (!dateText) return '—'
  const [year, month, day] = dateText.split('-')
  return `${day}/${month}/${year}`
}

function TarefasPage({ currentUser, onLogout, onNavigate }) {
  const [tarefas, setTarefas] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [usuarios, setUsuarios] = useState([])
  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [modalFeedback, setModalFeedback] = useState('')

  const fetchTarefas = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const lista = await listarMinhasTarefas()
      setTarefas(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar as tarefas.' })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchTarefas()
  }, [fetchTarefas])

  function openModal() {
    setForm(defaultForm)
    setModalFeedback('')
    setModalOpen(true)
    listarUsuariosResumo()
      .then(setUsuarios)
      .catch(() => setUsuarios([]))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSaving(true)
    setModalFeedback('')
    try {
      await atribuirTarefa(form)
      setModalOpen(false)
      setFeedback({ type: 'info', message: 'Tarefa atribuída com sucesso.' })
      await fetchTarefas()
    } catch (error) {
      setModalFeedback(error.message || 'Falha ao atribuir a tarefa.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleConcluir(tarefa) {
    try {
      await concluirTarefa(tarefa.id, !tarefa.statusConclusao)
      await fetchTarefas()
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao atualizar a tarefa.' })
    }
  }

  async function handleExcluir(tarefa) {
    const confirmar = window.confirm(`Deseja excluir a tarefa "${tarefa.descricao}"?`)
    if (!confirmar) return
    try {
      await excluirTarefa(tarefa.id)
      await fetchTarefas()
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao excluir a tarefa.' })
    }
  }

  const pendentes = tarefas.filter((t) => !t.statusConclusao)
  const concluidas = tarefas.filter((t) => t.statusConclusao)

  function renderLinha(tarefa) {
    const souAtribuidor = tarefa.atribuidoPorEmail?.toLowerCase() === currentUser.email?.toLowerCase()
    return (
      <tr key={tarefa.id}>
        <td>{tarefa.descricao}</td>
        <td>{formatDate(tarefa.dataLimite)}</td>
        <td>
          {souAtribuidor ? `Para: ${tarefa.atribuidoParaNome || tarefa.atribuidoParaEmail}` : `De: ${tarefa.atribuidoPorNome || tarefa.atribuidoPorEmail}`}
        </td>
        <td>
          <div className="row-actions">
            <button type="button" className="btn-row" onClick={() => handleConcluir(tarefa)}>
              {tarefa.statusConclusao ? 'Reabrir' : 'Concluir'}
            </button>
            <button type="button" className="btn-row btn-row--danger" onClick={() => handleExcluir(tarefa)}>
              Excluir
            </button>
          </div>
        </td>
      </tr>
    )
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
          <button type="button" className="menu-item" onClick={() => onNavigate('metas')}>
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
          <button type="button" className="menu-item menu-item--active">
            Tarefas
          </button>
          {currentUser.perfil === 'ADMINISTRADOR' ? (
            <button type="button" className="menu-item" onClick={() => onNavigate('configuracoes')}>
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
          <h1>Tarefas</h1>
          <span>Sessão ativa: {currentUser.nome}</span>
        </header>

        {feedback.message ? (
          <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
            {feedback.message}
          </p>
        ) : null}

        <div className="data-toolbar">
          <p className="animals-count">
            {isLoading ? 'Carregando...' : `${pendentes.length} pendente(s), ${concluidas.length} concluída(s)`}
          </p>
          <button type="button" className="btn-new-entity" onClick={openModal}>
            + Atribuir tarefa
          </button>
        </div>

        <h3 className="details-section">Pendentes</h3>
        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Descrição</th>
                <th>Prazo</th>
                <th>Origem/Destino</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={4} className="table-loading">Carregando...</td></tr>
              ) : pendentes.length === 0 ? (
                <tr><td colSpan={4} className="table-empty">Nenhuma tarefa pendente.</td></tr>
              ) : (
                pendentes.map(renderLinha)
              )}
            </tbody>
          </table>
        </div>

        <h3 className="details-section">Concluídas</h3>
        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Descrição</th>
                <th>Prazo</th>
                <th>Origem/Destino</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={4} className="table-loading">Carregando...</td></tr>
              ) : concluidas.length === 0 ? (
                <tr><td colSpan={4} className="table-empty">Nenhuma tarefa concluída.</td></tr>
              ) : (
                concluidas.map(renderLinha)
              )}
            </tbody>
          </table>
        </div>
      </section>

      {modalOpen ? (
        <AtribuirTarefaModal
          formData={form}
          usuarios={usuarios}
          isSaving={isSaving}
          feedback={modalFeedback}
          onClose={() => setModalOpen(false)}
          onChange={(e) => setForm((c) => ({ ...c, [e.target.name]: e.target.value }))}
          onSubmit={handleSubmit}
        />
      ) : null}
    </main>
  )
}

export default TarefasPage
