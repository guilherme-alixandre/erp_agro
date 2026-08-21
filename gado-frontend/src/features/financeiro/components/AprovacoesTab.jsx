import { useCallback, useEffect, useState } from 'react'
import { aprovarDocumento, listarPendentesAprovacao, recusarDocumento } from '../integration/documentoEntradaApi'
import RecusarDocumentoModal from './RecusarDocumentoModal'
import AprovarDocumentoModal from './AprovarDocumentoModal'

const PERFIS_GERENCIAIS = ['ADMINISTRADOR', 'GERENTE']

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

function AprovacoesTab({ currentUser }) {
  const isGerencial = PERFIS_GERENCIAIS.includes(currentUser?.perfil)

  const [pendentes, setPendentes] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })
  const [aprovandoId, setAprovandoId] = useState(null)

  const [recusaAlvo, setRecusaAlvo] = useState(null)
  const [justificativa, setJustificativa] = useState('')
  const [isRecusando, setIsRecusando] = useState(false)
  const [recusaFeedback, setRecusaFeedback] = useState('')

  const [aprovacaoAlvo, setAprovacaoAlvo] = useState(null)
  const [aprovacaoFeedback, setAprovacaoFeedback] = useState('')

  const fetchPendentes = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const lista = await listarPendentesAprovacao(currentUser.email)
      setPendentes(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar as pendências.' })
    } finally {
      setIsLoading(false)
    }
  }, [currentUser.email])

  useEffect(() => {
    fetchPendentes()
  }, [fetchPendentes])

  function abrirAprovacao(documento) {
    setAprovacaoAlvo(documento)
    setAprovacaoFeedback('')
  }

  function fecharAprovacao() {
    setAprovacaoAlvo(null)
    setAprovacaoFeedback('')
  }

  async function handleAprovar() {
    const documento = aprovacaoAlvo
    setAprovandoId(documento.id)
    setAprovacaoFeedback('')
    setFeedback({ type: '', message: '' })
    try {
      await aprovarDocumento(documento.id, currentUser.email)
      setFeedback({ type: 'info', message: 'Documento aprovado com sucesso.' })
      fecharAprovacao()
      await fetchPendentes()
    } catch (error) {
      setAprovacaoFeedback(error.message || 'Falha ao aprovar o documento.')
    } finally {
      setAprovandoId(null)
    }
  }

  function abrirRecusa(documento) {
    setRecusaAlvo(documento)
    setJustificativa('')
    setRecusaFeedback('')
  }

  function fecharRecusa() {
    setRecusaAlvo(null)
    setJustificativa('')
    setRecusaFeedback('')
  }

  async function handleRecusar(event) {
    event.preventDefault()
    setIsRecusando(true)
    setRecusaFeedback('')
    try {
      await recusarDocumento(recusaAlvo.id, currentUser.email, justificativa)
      fecharRecusa()
      await fetchPendentes()
    } catch (error) {
      setRecusaFeedback(error.message || 'Falha ao recusar o documento.')
    } finally {
      setIsRecusando(false)
    }
  }

  return (
    <>
      {feedback.message ? (
        <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
          {feedback.message}
        </p>
      ) : null}

      {!isGerencial ? (
        <p className="form-info">
          Apenas Administrador ou Gerente podem aprovar ou recusar. Você pode acompanhar as
          pendências abaixo.
        </p>
      ) : null}

      <div className="data-toolbar">
        <p className="animals-count">
          {isLoading ? 'Carregando...' : `${pendentes.length} pendente(s) de aprovação`}
        </p>
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Descrição</th>
              <th>Valor</th>
              <th>Data emissão</th>
              <th>Criado por</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={5} className="table-loading">Carregando...</td>
              </tr>
            ) : pendentes.length === 0 ? (
              <tr>
                <td colSpan={5} className="table-empty">Nenhum documento pendente de aprovação.</td>
              </tr>
            ) : (
              pendentes.map((doc) => (
                <tr key={doc.id}>
                  <td>{doc.itens?.[0]?.descricaoXml || doc.numeroDocumento || '—'}</td>
                  <td>R$ {Number(doc.valorTotal ?? 0).toFixed(2)}</td>
                  <td>{formatarData(doc.dataEmissao)}</td>
                  <td>{doc.criadoPorEmail}</td>
                  <td>
                    {isGerencial ? (
                      <div className="row-actions">
                        <button
                          type="button"
                          className="btn-row"
                          disabled={aprovandoId === doc.id}
                          onClick={() => abrirAprovacao(doc)}
                        >
                          {aprovandoId === doc.id ? 'Aprovando...' : 'Aprovar'}
                        </button>
                        <button type="button" className="btn-row btn-row--danger" onClick={() => abrirRecusa(doc)}>
                          Recusar
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

      {recusaAlvo ? (
        <RecusarDocumentoModal
          documento={recusaAlvo}
          justificativa={justificativa}
          isSaving={isRecusando}
          feedback={recusaFeedback}
          onClose={fecharRecusa}
          onChange={(e) => setJustificativa(e.target.value)}
          onSubmit={handleRecusar}
        />
      ) : null}

      {aprovacaoAlvo ? (
        <AprovarDocumentoModal
          documento={aprovacaoAlvo}
          isSaving={aprovandoId === aprovacaoAlvo.id}
          feedback={aprovacaoFeedback}
          onClose={fecharAprovacao}
          onConfirm={handleAprovar}
        />
      ) : null}
    </>
  )
}

export default AprovacoesTab
