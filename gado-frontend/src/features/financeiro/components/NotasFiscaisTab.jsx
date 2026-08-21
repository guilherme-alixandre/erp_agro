import { useCallback, useEffect, useState } from 'react'
import {
  cadastrarReciboSimples,
  editarNfe,
  importarNfeXml,
  listarDocumentos,
  vincularProduto,
} from '../integration/documentoEntradaApi'
import ImportarNfeModal from './ImportarNfeModal'
import ReciboSimplesFormModal from './ReciboSimplesFormModal'
import VincularItensModal from './VincularItensModal'
import EditarNfeModal from './EditarNfeModal'

const PERFIS_GERENCIAIS = ['ADMINISTRADOR', 'GERENTE']

const STATUS_LABEL = {
  APROVADO: 'Aprovado',
  PENDENTE_APROVACAO: 'Pendente',
  RECUSADO: 'Recusado',
}

const STATUS_CLASS = {
  APROVADO: 'financeiro-status--aprovado',
  PENDENTE_APROVACAO: 'financeiro-status--pendente',
  RECUSADO: 'financeiro-status--recusado',
}

const defaultReciboForm = { descricao: '', dataEmissao: '', valorTotal: '', naturezaFinanceira: '' }

/**
 * Formata uma data-only ISO ("2026-08-20") sem passar por Date — `new Date(iso)` interpreta
 * strings sem hora como UTC meia-noite, e toLocaleDateString depois renderiza no fuso local,
 * o que mostra um dia a menos em fusos atrás de UTC (ex.: Brasil).
 */
function formatarData(iso) {
  if (!iso) return '—'
  const [ano, mes, dia] = String(iso).split('-')
  return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso
}

function NotasFiscaisTab({ currentUser }) {
  const isGerencial = PERFIS_GERENCIAIS.includes(currentUser?.perfil)

  const [documentos, setDocumentos] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [modal, setModal] = useState({ type: null, documento: null })
  const [isSaving, setIsSaving] = useState(false)
  const [modalFeedback, setModalFeedback] = useState('')
  const [reciboForm, setReciboForm] = useState(defaultReciboForm)

  const [vinculandoItemId, setVinculandoItemId] = useState(null)
  const [feedbackPorItem, setFeedbackPorItem] = useState({})

  const fetchDocumentos = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const lista = await listarDocumentos(currentUser.email)
      setDocumentos(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar os documentos.' })
    } finally {
      setIsLoading(false)
    }
  }, [currentUser.email])

  useEffect(() => {
    fetchDocumentos()
  }, [fetchDocumentos])

  function closeModal() {
    setModal({ type: null, documento: null })
    setModalFeedback('')
    setReciboForm(defaultReciboForm)
    setFeedbackPorItem({})
  }

  async function handleImportarXml(file) {
    setIsSaving(true)
    setModalFeedback('')
    try {
      await importarNfeXml(currentUser.email, file)
      setFeedback({ type: 'info', message: 'NF-e importada e aprovada com sucesso.' })
      closeModal()
      await fetchDocumentos()
    } catch (error) {
      setModalFeedback(error.message || 'Falha ao importar o XML.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleCadastrarRecibo(event) {
    event.preventDefault()
    setIsSaving(true)
    setModalFeedback('')
    try {
      await cadastrarReciboSimples(currentUser.email, reciboForm)
      setFeedback({ type: 'info', message: 'Recibo cadastrado — aguardando aprovação.' })
      closeModal()
      await fetchDocumentos()
    } catch (error) {
      setModalFeedback(error.message || 'Falha ao cadastrar o recibo.')
    } finally {
      setIsSaving(false)
    }
  }

  async function handleVincular(itemId, produtoId) {
    setVinculandoItemId(itemId)
    setFeedbackPorItem((current) => ({ ...current, [itemId]: '' }))
    try {
      await vincularProduto(itemId, currentUser.email, produtoId)
      const atualizados = await listarDocumentos(currentUser.email)
      setDocumentos(atualizados)
      const documentoAtualizado = atualizados.find((d) => d.id === modal.documento.id)
      setModal((current) => ({ ...current, documento: documentoAtualizado ?? current.documento }))
    } catch (error) {
      setFeedbackPorItem((current) => ({ ...current, [itemId]: error.message || 'Falha ao vincular.' }))
    } finally {
      setVinculandoItemId(null)
    }
  }

  async function handleEditarNfe(payload) {
    setIsSaving(true)
    setModalFeedback('')
    try {
      await editarNfe(modal.documento.id, currentUser.email, payload)
      setFeedback({ type: 'info', message: 'NF-e corrigida com sucesso.' })
      closeModal()
      await fetchDocumentos()
    } catch (error) {
      setModalFeedback(error.message || 'Falha ao salvar a correção.')
    } finally {
      setIsSaving(false)
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
          {isLoading ? 'Carregando...' : `${documentos.length} documento(s)`}
        </p>
        <div className="row-actions">
          <button type="button" className="btn-secondary" onClick={() => setModal({ type: 'recibo', documento: null })}>
            + Novo Recibo
          </button>
          <button type="button" className="btn-new-entity" onClick={() => setModal({ type: 'importar', documento: null })}>
            + Importar XML (NF-e)
          </button>
        </div>
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Tipo</th>
              <th>Número</th>
              <th>Data Emissão</th>
              <th>Valor Total</th>
              <th>Itens vinculados</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={7} className="table-loading">Carregando...</td>
              </tr>
            ) : documentos.length === 0 ? (
              <tr>
                <td colSpan={7} className="table-empty">Nenhum documento de entrada cadastrado.</td>
              </tr>
            ) : (
              documentos.map((doc) => {
                const vinculados = doc.itens.filter((i) => i.vinculado).length
                return (
                  <tr key={doc.id}>
                    <td>{doc.tipoDocumento === 'NF_E' ? 'NF-e' : 'Recibo'}</td>
                    <td>{doc.numeroDocumento || '—'}</td>
                    <td>{formatarData(doc.dataEmissao)}</td>
                    <td>R$ {Number(doc.valorTotal ?? 0).toFixed(2)}</td>
                    <td>{vinculados}/{doc.itens.length}</td>
                    <td>
                      <span className={`financeiro-status ${STATUS_CLASS[doc.statusAprovacao] ?? ''}`}>
                        {STATUS_LABEL[doc.statusAprovacao] ?? doc.statusAprovacao}
                      </span>
                    </td>
                    <td>
                      <div className="row-actions">
                        <button
                          type="button"
                          className="btn-row"
                          onClick={() => setModal({ type: 'vincular', documento: doc })}
                        >
                          Itens
                        </button>
                        {doc.tipoDocumento === 'NF_E' && isGerencial ? (
                          <button
                            type="button"
                            className="btn-row btn-row--edit"
                            onClick={() => setModal({ type: 'editar', documento: doc })}
                          >
                            Editar
                          </button>
                        ) : null}
                      </div>
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      {modal.type === 'importar' ? (
        <ImportarNfeModal isSaving={isSaving} feedback={modalFeedback} onClose={closeModal} onSubmit={handleImportarXml} />
      ) : null}

      {modal.type === 'recibo' ? (
        <ReciboSimplesFormModal
          formData={reciboForm}
          isSaving={isSaving}
          feedback={modalFeedback}
          onClose={closeModal}
          onChange={(e) => setReciboForm((c) => ({ ...c, [e.target.name]: e.target.value }))}
          onSubmit={handleCadastrarRecibo}
        />
      ) : null}

      {modal.type === 'vincular' && modal.documento ? (
        <VincularItensModal
          documento={modal.documento}
          onClose={closeModal}
          onVincular={handleVincular}
          vinculandoItemId={vinculandoItemId}
          feedbackPorItem={feedbackPorItem}
        />
      ) : null}

      {modal.type === 'editar' && modal.documento ? (
        <EditarNfeModal
          documento={modal.documento}
          isSaving={isSaving}
          feedback={modalFeedback}
          onClose={closeModal}
          onSubmit={handleEditarNfe}
        />
      ) : null}
    </>
  )
}

export default NotasFiscaisTab
