import { useCallback, useEffect, useState } from 'react'
import { listarMinhasTarefas, atribuirTarefa, concluirTarefa, excluirTarefa } from '../integration/tarefaApi'
import { listarUsuariosResumo } from '../integration/usuarioResumoApi'
import AtribuirTarefaModal from '../components/AtribuirTarefaModal'
import ModuleHeader from '../../../components/shared/ModuleHeader'
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

  function renderCard(tarefa) {
    const souAtribuidor = tarefa.atribuidoPorEmail?.toLowerCase() === currentUser.email?.toLowerCase()
    const prazo = tarefa.dataLimite ? new Date(`${tarefa.dataLimite}T23:59:59`) : null
    const atrasada = !tarefa.statusConclusao && prazo && prazo < new Date()
    return (
      <article className={`task-card${tarefa.statusConclusao ? ' task-card--done' : ''}`} key={tarefa.id}>
        <button
          type="button"
          className="task-card__check"
          onClick={() => handleConcluir(tarefa)}
          aria-label={tarefa.statusConclusao ? 'Reabrir tarefa' : 'Concluir tarefa'}
          title={tarefa.statusConclusao ? 'Reabrir tarefa' : 'Concluir tarefa'}
        >
          {tarefa.statusConclusao ? '✓' : ''}
        </button>
        <div className="task-card__body">
          <strong>{tarefa.descricao}</strong>
          <div className="task-card__meta">
            <span className={atrasada ? 'task-card__due task-card__due--late' : 'task-card__due'}>
              {atrasada ? 'Atrasada · ' : 'Prazo · '}{formatDate(tarefa.dataLimite)}
            </span>
            <span>
              {souAtribuidor ? `Para ${tarefa.atribuidoParaNome || tarefa.atribuidoParaEmail}` : `De ${tarefa.atribuidoPorNome || tarefa.atribuidoPorEmail}`}
            </span>
          </div>
        </div>
        <button
          type="button"
          className="task-card__delete"
          onClick={() => handleExcluir(tarefa)}
          aria-label="Excluir tarefa"
          title="Excluir tarefa"
        >
          ×
        </button>
      </article>
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
        <ModuleHeader
          icon="check"
          title="Tarefas da equipe"
          description="Uma lista de trabalho direta para saber o que fazer, para quem e até quando."
          metrics={[
            { value: pendentes.length, label: 'Pendentes' },
            { value: concluidas.length, label: 'Concluídas' },
            { value: tarefas.length, label: 'Total' },
          ]}
        />

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

        <div className="task-board">
          <section className="task-column">
            <header className="task-column__header">
              <span className="task-column__dot task-column__dot--pending" />
              <h2>Para fazer</h2>
              <strong>{pendentes.length}</strong>
            </header>
            <div className="task-column__list">
              {isLoading ? (
                <p className="task-column__empty">Carregando tarefas...</p>
              ) : pendentes.length === 0 ? (
                <p className="task-column__empty">Tudo em dia por aqui.</p>
              ) : pendentes.map(renderCard)}
            </div>
          </section>

          <section className="task-column task-column--done">
            <header className="task-column__header">
              <span className="task-column__dot task-column__dot--done" />
              <h2>Concluídas</h2>
              <strong>{concluidas.length}</strong>
            </header>
            <div className="task-column__list">
              {isLoading ? (
                <p className="task-column__empty">Carregando tarefas...</p>
              ) : concluidas.length === 0 ? (
                <p className="task-column__empty">As tarefas finalizadas aparecem aqui.</p>
              ) : concluidas.map(renderCard)}
            </div>
          </section>
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
