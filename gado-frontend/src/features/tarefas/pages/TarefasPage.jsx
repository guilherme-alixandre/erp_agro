import { useCallback, useEffect, useState } from 'react'
import {
  listarMinhasTarefas,
  listarTarefasAtribuidasPorMim,
  atribuirTarefa,
  editarTarefa,
  concluirTarefa,
  excluirTarefa,
} from '../integration/tarefaApi'
import { listarUsuariosResumo } from '../integration/usuarioResumoApi'
import AtribuirTarefaModal from '../components/AtribuirTarefaModal'
import EditarTarefaModal from '../components/EditarTarefaModal'
import ModuleHeader from '../../../components/shared/ModuleHeader'
import { formatarData, hojeIso } from '../../../utils/formatters'
import '../../animais/styles/animais.css'

const defaultForm = { descricao: '', dataLimite: '', atribuidoParaEmail: '' }
const defaultEditForm = { descricao: '', dataLimite: '', statusConclusao: false }

function TarefasPage({ currentUser, onLogout, onNavigate }) {
  const [tarefas, setTarefas] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [tarefasAtribuidas, setTarefasAtribuidas] = useState([])
  const [isLoadingAtribuidas, setIsLoadingAtribuidas] = useState(false)

  const [usuarios, setUsuarios] = useState([])
  const [modalOpen, setModalOpen] = useState(false)
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [modalFeedback, setModalFeedback] = useState('')

  const [editModal, setEditModal] = useState({ open: false, tarefa: null })
  const [editForm, setEditForm] = useState(defaultEditForm)
  const [isSavingEdit, setIsSavingEdit] = useState(false)
  const [editFeedback, setEditFeedback] = useState('')

  const fetchTarefas = useCallback(async () => {
    setIsLoading(true)
    setFeedback((atual) => (atual.type === 'error' ? { type: '', message: '' } : atual))
    try {
      const lista = await listarMinhasTarefas()
      setTarefas(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar as tarefas.' })
    } finally {
      setIsLoading(false)
    }
  }, [])

  const fetchTarefasAtribuidas = useCallback(async () => {
    setIsLoadingAtribuidas(true)
    try {
      const lista = await listarTarefasAtribuidasPorMim()
      setTarefasAtribuidas(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar as tarefas atribuídas.' })
    } finally {
      setIsLoadingAtribuidas(false)
    }
  }, [])

  useEffect(() => {
    fetchTarefas()
    fetchTarefasAtribuidas()
  }, [fetchTarefas, fetchTarefasAtribuidas])

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
      await Promise.all([fetchTarefas(), fetchTarefasAtribuidas()])
    } catch (error) {
      setModalFeedback(error.message || 'Falha ao atribuir a tarefa.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleConcluir(tarefa) {
    try {
      await concluirTarefa(tarefa.id, !tarefa.statusConclusao)
      await Promise.all([fetchTarefas(), fetchTarefasAtribuidas()])
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao atualizar a tarefa.' })
    }
  }

  function openEditModal(tarefa) {
    setEditModal({ open: true, tarefa })
    setEditForm({
      descricao: tarefa.descricao,
      dataLimite: tarefa.dataLimite ? String(tarefa.dataLimite).slice(0, 10) : '',
      statusConclusao: tarefa.statusConclusao,
    })
    setEditFeedback('')
  }

  function closeEditModal() {
    setEditModal({ open: false, tarefa: null })
    setEditForm(defaultEditForm)
    setEditFeedback('')
  }

  function handleEditChange(event) {
    const { name, value, type, checked } = event.target
    setEditForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }))
  }

  async function handleEditSubmit(event) {
    event.preventDefault()
    setIsSavingEdit(true)
    setEditFeedback('')
    try {
      await editarTarefa(editModal.tarefa.id, {
        descricao: editForm.descricao.trim(),
        dataLimite: editForm.dataLimite || null,
        statusConclusao: editForm.statusConclusao,
      })
      closeEditModal()
      await Promise.all([fetchTarefas(), fetchTarefasAtribuidas()])
    } catch (error) {
      setEditFeedback(error.message || 'Falha ao salvar as alterações.')
    } finally {
      setIsSavingEdit(false)
    }
  }

  async function handleExcluir(tarefa) {
    const confirmar = window.confirm(`Deseja excluir a tarefa "${tarefa.descricao}"?`)
    if (!confirmar) return
    try {
      await excluirTarefa(tarefa.id)
      await Promise.all([fetchTarefas(), fetchTarefasAtribuidas()])
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao excluir a tarefa.' })
    }
  }

  const pendentes = tarefas.filter((t) => !t.statusConclusao)
  const concluidas = tarefas.filter((t) => t.statusConclusao)

  // Tarefas que este usuário delegou a outra pessoa (as que ele atribuiu a si mesmo já aparecem acima).
  const delegadas = tarefasAtribuidas.filter(
    (t) => t.atribuidoParaEmail?.toLowerCase() !== currentUser.email?.toLowerCase(),
  )

  function renderCard(tarefa) {
    const souAtribuidor = tarefa.atribuidoPorEmail?.toLowerCase() === currentUser.email?.toLowerCase()
    // dataLimite chega como java.util.Date ("2026-09-20T00:00:00.000Z"): o que vale é o dia, não o instante.
    const diaLimite = tarefa.dataLimite ? String(tarefa.dataLimite).slice(0, 10) : null
    const atrasada = !tarefa.statusConclusao && diaLimite !== null && diaLimite < hojeIso()
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
              {diaLimite ? `${atrasada ? 'Atrasada' : 'Prazo'} · ${formatarData(diaLimite)}` : 'Sem prazo'}
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

  function renderDelegatedCard(tarefa) {
    const diaLimite = tarefa.dataLimite ? String(tarefa.dataLimite).slice(0, 10) : null
    const atrasada = !tarefa.statusConclusao && diaLimite !== null && diaLimite < hojeIso()
    return (
      <article className={`task-card${tarefa.statusConclusao ? ' task-card--done' : ''}`} key={tarefa.id}>
        <span
          className={`task-status-badge ${tarefa.statusConclusao ? 'task-status-badge--done' : 'task-status-badge--pending'}`}
          title={tarefa.statusConclusao ? 'Concluída' : 'Pendente'}
        >
          {tarefa.statusConclusao ? '✓' : '•'}
        </span>
        <div className="task-card__body">
          <strong>{tarefa.descricao}</strong>
          <div className="task-card__meta">
            <span className={atrasada ? 'task-card__due task-card__due--late' : 'task-card__due'}>
              {diaLimite ? `${atrasada ? 'Atrasada' : 'Prazo'} · ${formatarData(diaLimite)}` : 'Sem prazo'}
            </span>
            <span>Para {tarefa.atribuidoParaNome || tarefa.atribuidoParaEmail}</span>
            <span>{tarefa.statusConclusao ? 'Concluída' : 'Pendente'}</span>
          </div>
        </div>
        <div className="task-card__actions">
          <button
            type="button"
            className="btn-row btn-row--edit"
            onClick={() => openEditModal(tarefa)}
          >
            Editar
          </button>
          <button
            type="button"
            className="task-card__delete"
            onClick={() => handleExcluir(tarefa)}
            aria-label="Excluir tarefa"
            title="Excluir tarefa"
          >
            ×
          </button>
        </div>
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

        <section className="task-board--delegated">
          <header className="task-column__header">
            <span className="task-column__dot task-column__dot--pending" />
            <h2>Tarefas que você atribuiu a outras pessoas</h2>
            <strong>{delegadas.length}</strong>
          </header>
          <div className="task-column__list">
            {isLoadingAtribuidas ? (
              <p className="task-column__empty">Carregando tarefas...</p>
            ) : delegadas.length === 0 ? (
              <p className="task-column__empty">Você ainda não atribuiu tarefas a outras pessoas.</p>
            ) : delegadas.map(renderDelegatedCard)}
          </div>
        </section>
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

      {editModal.open ? (
        <EditarTarefaModal
          formData={editForm}
          isSaving={isSavingEdit}
          feedback={editFeedback}
          onClose={closeEditModal}
          onChange={handleEditChange}
          onSubmit={handleEditSubmit}
        />
      ) : null}
    </main>
  )
}

export default TarefasPage
