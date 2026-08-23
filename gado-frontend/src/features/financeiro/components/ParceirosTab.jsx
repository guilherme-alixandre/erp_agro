import { useCallback, useEffect, useState } from 'react'
import { listarParceiros, cadastrarParceiro, atualizarParceiro, excluirParceiro } from '../integration/parceiroApi'
import ParceiroFormModal from './ParceiroFormModal'

const PERFIS_GERENCIAIS = ['ADMINISTRADOR', 'GERENTE']

const TIPO_LABEL = { FORNECEDOR: 'Fornecedor', COMPRADOR: 'Comprador', AMBOS: 'Ambos' }

const defaultForm = { nome: '', cpfCnpj: '', email: '', telefone: '', endereco: '', tipo: '' }

function ParceirosTab({ currentUser }) {
  const isGerencial = PERFIS_GERENCIAIS.includes(currentUser?.perfil)

  const [parceiros, setParceiros] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [modalOpen, setModalOpen] = useState(false)
  const [formMode, setFormMode] = useState('create')
  const [editandoCpfCnpj, setEditandoCpfCnpj] = useState(null)
  const [form, setForm] = useState(defaultForm)
  const [isSaving, setIsSaving] = useState(false)
  const [modalFeedback, setModalFeedback] = useState('')

  const fetchParceiros = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const lista = await listarParceiros()
      setParceiros(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar os parceiros.' })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchParceiros()
  }, [fetchParceiros])

  function openCreateModal() {
    setFormMode('create')
    setEditandoCpfCnpj(null)
    setForm(defaultForm)
    setModalFeedback('')
    setModalOpen(true)
  }

  function openEditModal(parceiro) {
    setFormMode('edit')
    setEditandoCpfCnpj(parceiro.cpfCnpj)
    setForm({
      nome: parceiro.nome,
      cpfCnpj: parceiro.cpfCnpj,
      email: parceiro.email,
      telefone: parceiro.telefone,
      endereco: parceiro.endereco,
      tipo: parceiro.tipo,
    })
    setModalFeedback('')
    setModalOpen(true)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSaving(true)
    setModalFeedback('')
    try {
      if (formMode === 'edit') {
        await atualizarParceiro(editandoCpfCnpj, form)
        setFeedback({ type: 'info', message: 'Parceiro atualizado com sucesso.' })
      } else {
        await cadastrarParceiro(form)
        setFeedback({ type: 'info', message: 'Parceiro cadastrado com sucesso.' })
      }
      setModalOpen(false)
      await fetchParceiros()
    } catch (error) {
      setModalFeedback(error.message || 'Falha ao salvar o parceiro.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleExcluir(parceiro) {
    const confirmar = window.confirm(`Deseja excluir o parceiro ${parceiro.nome}?`)
    if (!confirmar) return
    try {
      await excluirParceiro(parceiro.cpfCnpj)
      setFeedback({ type: 'info', message: 'Parceiro excluído com sucesso.' })
      await fetchParceiros()
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao excluir o parceiro.' })
    }
  }

  return (
    <>
      {feedback.message ? (
        <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
          {feedback.message}
        </p>
      ) : null}

      <div className="data-toolbar">
        <p className="animals-count">
          {isLoading ? 'Carregando...' : `${parceiros.length} parceiro(s)`}
        </p>
        {isGerencial ? (
          <button type="button" className="btn-new-entity" onClick={openCreateModal}>
            + Novo Parceiro
          </button>
        ) : null}
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Nome</th>
              <th>CPF/CNPJ</th>
              <th>E-mail</th>
              <th>Tipo</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={5} className="table-loading">Carregando...</td></tr>
            ) : parceiros.length === 0 ? (
              <tr><td colSpan={5} className="table-empty">Nenhum parceiro cadastrado.</td></tr>
            ) : (
              parceiros.map((p) => (
                <tr key={p.cpfCnpj}>
                  <td>{p.nome}</td>
                  <td>{p.cpfCnpj}</td>
                  <td>{p.email || '—'}</td>
                  <td>{TIPO_LABEL[p.tipo] ?? p.tipo}</td>
                  <td>
                    {isGerencial ? (
                      <div className="row-actions">
                        <button type="button" className="btn-row btn-row--edit" onClick={() => openEditModal(p)}>
                          Editar
                        </button>
                        <button type="button" className="btn-row btn-row--danger" onClick={() => handleExcluir(p)}>
                          Excluir
                        </button>
                      </div>
                    ) : (
                      <span>—</span>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {modalOpen ? (
        <ParceiroFormModal
          mode={formMode}
          formData={form}
          isSaving={isSaving}
          feedback={modalFeedback}
          onClose={() => setModalOpen(false)}
          onChange={(e) => setForm((c) => ({ ...c, [e.target.name]: e.target.value }))}
          onSubmit={handleSubmit}
        />
      ) : null}
    </>
  )
}

export default ParceirosTab
