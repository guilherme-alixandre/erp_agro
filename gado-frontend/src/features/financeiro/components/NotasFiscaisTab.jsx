import { useCallback, useEffect, useState } from 'react'
import {
  cadastrarReciboSimples,
  editarNfe,
  excluirDocumento,
  importarNfeXml,
  listarDocumentos,
  vincularProduto,
} from '../integration/documentoEntradaApi'
import { cadastrarVendaAnimal, cadastrarVendaLeite } from '../integration/documentoSaidaApi'
import { listarNotasFiscais } from '../integration/notaFiscalApi'
import { cadastrarInsumoEstoque, listarEstoque } from '../../insumos/integration/insumoApi'
import { listarUnidadesMedida } from '../../insumos/integration/unidadeMedidaApi'
import { listarGruposProduto } from '../../insumos/integration/grupoProdutoApi'
import { listarLotes, listarAnimaisParaLote } from '../../lotes/integration/loteApi'
import { listarParceiros } from '../integration/parceiroApi'
import ImportarNfeModal from './ImportarNfeModal'
import ReciboSimplesFormModal from './ReciboSimplesFormModal'
import VincularItensModal from './VincularItensModal'
import EditarNfeModal from './EditarNfeModal'
import InsumoEstoqueFormModal from '../../insumos/components/InsumoEstoqueFormModal'
import RegistrarVendaModal from './RegistrarVendaModal'

const PERFIS_GERENCIAIS = ['ADMINISTRADOR', 'GERENTE']
const PERFIS_EXCLUSAO_NF = ['ADMINISTRADOR', 'GERENTE', 'FINANCEIRO']

const TIPO_DOC_LABEL = {
  NF_E: 'NF-e',
  RECIBO_SIMPLES: 'Recibo',
  VENDA_LEITE: 'Venda de leite',
  VENDA_ANIMAL: 'Venda/abate de animais',
}

const defaultEstoqueForm = {
  nome: '', tipo: 'RACAO', grupoProdutoId: '', grupoProdutoNome: '', codigoProduto: '',
  unidadeMedidaPrimariaId: '', unidadeMedidaPrimariaSigla: '', unidadeMedidaSecundariaId: '',
  fatorConversao: '', estoqueMinimo: '', precoCompraMedio: '', precoUltimaCompra: '',
}

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

const defaultReciboForm = {
  descricao: '', dataEmissao: '', produtoId: '', quantidade: '', valorTotal: '', naturezaFinanceira: '',
}

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

  const [produtos, setProdutos] = useState([])
  const [unidades, setUnidades] = useState([])
  const [grupos, setGrupos] = useState([])
  const [criarProdutoModalOpen, setCriarProdutoModalOpen] = useState(false)
  const [novoProdutoForm, setNovoProdutoForm] = useState(defaultEstoqueForm)
  const [novoProdutoFeedback, setNovoProdutoFeedback] = useState('')
  const [isSavingProduto, setIsSavingProduto] = useState(false)

  const [isExcluindo, setIsExcluindo] = useState(false)

  const [notasFiscais, setNotasFiscais] = useState([])
  const [numeroInput, setNumeroInput] = useState('')
  const [chaveInput, setChaveInput] = useState('')
  const [filtros, setFiltros] = useState({ numero: '', chave: '', direcao: '' })

  const [lotes, setLotes] = useState([])
  const [animaisDisponiveis, setAnimaisDisponiveis] = useState([])
  const [fornecedores, setFornecedores] = useState([])
  const [compradores, setCompradores] = useState([])
  const [vendaModalOpen, setVendaModalOpen] = useState(false)
  const [isSavingVenda, setIsSavingVenda] = useState(false)
  const [vendaFeedback, setVendaFeedback] = useState('')

  const fetchApoio = useCallback(async () => {
    listarEstoque('', 'ATIVO').then(setProdutos).catch(() => setProdutos([]))
    listarUnidadesMedida().then(setUnidades).catch(() => setUnidades([]))
    listarGruposProduto('', 'ATIVO').then(setGrupos).catch(() => setGrupos([]))
    listarLotes().then(setLotes).catch(() => setLotes([]))
    listarAnimaisParaLote().then(setAnimaisDisponiveis).catch(() => setAnimaisDisponiveis([]))
    listarParceiros('FORNECEDOR').then(setFornecedores).catch(() => setFornecedores([]))
    listarParceiros('COMPRADOR').then(setCompradores).catch(() => setCompradores([]))
  }, [])

  useEffect(() => {
    fetchApoio()
  }, [fetchApoio])

  const fetchDocumentos = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const [entradas, unificado] = await Promise.all([
        listarDocumentos(currentUser.email),
        listarNotasFiscais(currentUser.email, filtros),
      ])
      setDocumentos(entradas)
      setNotasFiscais(unificado)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar os documentos.' })
    } finally {
      setIsLoading(false)
    }
  }, [currentUser.email, filtros])

  function handleFiltrarSubmit(event) {
    event.preventDefault()
    setFiltros({ numero: numeroInput.trim(), chave: chaveInput.trim(), direcao: filtros.direcao })
  }

  function handleDirecaoChange(event) {
    setFiltros((c) => ({ ...c, direcao: event.target.value }))
  }

  function handleLimparFiltros() {
    setNumeroInput('')
    setChaveInput('')
    setFiltros({ numero: '', chave: '', direcao: '' })
  }

  useEffect(() => {
    fetchDocumentos()
  }, [fetchDocumentos])

  function closeModal() {
    setModal({ type: null, documento: null })
    setModalFeedback('')
    setReciboForm(defaultReciboForm)
    setFeedbackPorItem({})
  }

  async function handleImportarXml(file, fornecedorId) {
    setIsSaving(true)
    setModalFeedback('')
    try {
      await importarNfeXml(currentUser.email, file, fornecedorId)
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

  function abrirCriarProduto() {
    setNovoProdutoForm(defaultEstoqueForm)
    setNovoProdutoFeedback('')
    setCriarProdutoModalOpen(true)
  }

  async function handleSubmitNovoProduto(event) {
    event.preventDefault()
    setIsSavingProduto(true)
    setNovoProdutoFeedback('')
    try {
      const criado = await cadastrarInsumoEstoque(currentUser.email, novoProdutoForm)
      setCriarProdutoModalOpen(false)
      setProdutos((current) => [...current, criado])
      setReciboForm((current) => ({ ...current, produtoId: criado.id }))
    } catch (error) {
      setNovoProdutoFeedback(error.message || 'Falha ao cadastrar o produto.')
    } finally {
      setIsSavingProduto(false)
    }
  }

  async function handleExcluirDocumento(documento) {
    const confirmar = window.confirm(
      `Deseja excluir este documento (${documento.tipoDocumento === 'NF_E' ? 'NF-e' : 'Recibo'} ${documento.numeroDocumento || 's/nº'})? ` +
        'Isso reverte a entrada de estoque e o lançamento financeiro correspondentes.',
    )
    if (!confirmar) return

    setIsExcluindo(true)
    setFeedback({ type: '', message: '' })
    try {
      await excluirDocumento(documento.id, currentUser.email)
      setFeedback({ type: 'info', message: 'Documento excluído com sucesso.' })
      await fetchDocumentos()
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao excluir o documento.' })
    } finally {
      setIsExcluindo(false)
    }
  }

  function abrirVendaModal() {
    setVendaFeedback('')
    setVendaModalOpen(true)
  }

  async function handleSubmitVendaLeite(formData) {
    setIsSavingVenda(true)
    setVendaFeedback('')
    try {
      await cadastrarVendaLeite(currentUser.email, formData)
      setVendaModalOpen(false)
      setFeedback({ type: 'info', message: 'Venda de leite registrada com sucesso.' })
      await fetchDocumentos()
    } catch (error) {
      setVendaFeedback(error.message || 'Falha ao registrar a venda de leite.')
    } finally {
      setIsSavingVenda(false)
    }
  }

  async function handleSubmitVendaAnimal(formData) {
    setIsSavingVenda(true)
    setVendaFeedback('')
    try {
      await cadastrarVendaAnimal(currentUser.email, formData)
      setVendaModalOpen(false)
      setFeedback({
        type: 'info',
        message: formData.destino === 'ABATIDO' ? 'Abate registrado com sucesso.' : 'Venda de animais registrada com sucesso.',
      })
      await fetchDocumentos()
    } catch (error) {
      setVendaFeedback(error.message || 'Falha ao registrar a venda/abate.')
    } finally {
      setIsSavingVenda(false)
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
        <form className="toolbar-search" onSubmit={handleFiltrarSubmit}>
          <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
          <input
            type="text"
            value={numeroInput}
            onChange={(e) => setNumeroInput(e.target.value)}
            placeholder="Buscar por número da NF"
          />
        </form>
        <form className="toolbar-search" onSubmit={handleFiltrarSubmit}>
          <span className="toolbar-search__icon" aria-hidden="true">🔍</span>
          <input
            type="text"
            value={chaveInput}
            onChange={(e) => setChaveInput(e.target.value)}
            placeholder="Buscar por chave de acesso"
          />
        </form>
        <select className="toolbar-select" value={filtros.direcao} onChange={handleDirecaoChange}>
          <option value="">Entrada e Receita</option>
          <option value="ENTRADA">Só Entrada</option>
          <option value="RECEITA">Só Receita</option>
        </select>
        <button type="button" className="btn-secondary" onClick={handleFiltrarSubmit}>
          Filtrar
        </button>
        {(filtros.numero || filtros.chave || filtros.direcao) ? (
          <button type="button" className="btn-secondary" onClick={handleLimparFiltros}>
            Limpar
          </button>
        ) : null}
      </div>

      <div className="data-toolbar">
        <p className="animals-count">
          {isLoading ? 'Carregando...' : `${notasFiscais.length} documento(s)`}
        </p>
        <div className="row-actions">
          <button type="button" className="btn-secondary" onClick={() => setModal({ type: 'recibo', documento: null })}>
            + Novo Recibo
          </button>
          <button type="button" className="btn-new-entity" onClick={() => setModal({ type: 'importar', documento: null })}>
            + Importar XML (NF-e)
          </button>
          <button type="button" className="btn-new-entity" onClick={abrirVendaModal}>
            + Registrar Venda
          </button>
        </div>
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Direção</th>
              <th>Tipo</th>
              <th>Número</th>
              <th>Chave</th>
              <th>Data Emissão</th>
              <th>Valor Total</th>
              <th>Status</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={8} className="table-loading">Carregando...</td>
              </tr>
            ) : notasFiscais.length === 0 ? (
              <tr>
                <td colSpan={8} className="table-empty">Nenhuma nota fiscal encontrada.</td>
              </tr>
            ) : (
              notasFiscais.map((nf) => {
                const doc = nf.direcao === 'ENTRADA' ? documentos.find((d) => d.id === nf.id) : null
                return (
                  <tr key={`${nf.direcao}-${nf.id}`}>
                    <td>{nf.direcao === 'ENTRADA' ? 'Entrada' : 'Receita'}</td>
                    <td>{TIPO_DOC_LABEL[nf.tipoDocumento] ?? nf.tipoDocumento}</td>
                    <td>{nf.numeroDocumento || '—'}</td>
                    <td className="codigo-produto">{nf.chaveAcesso || '—'}</td>
                    <td>{formatarData(nf.dataEmissao)}</td>
                    <td>R$ {Number(nf.valorTotal ?? 0).toFixed(2)}</td>
                    <td>
                      {nf.direcao === 'ENTRADA' ? (
                        <span className={`financeiro-status ${STATUS_CLASS[nf.status] ?? ''}`}>
                          {STATUS_LABEL[nf.status] ?? nf.status}
                        </span>
                      ) : (
                        <span className="financeiro-status financeiro-status--aprovado">Confirmado</span>
                      )}
                    </td>
                    <td>
                      {doc ? (
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
                          {PERFIS_EXCLUSAO_NF.includes(currentUser?.perfil) ? (
                            <button
                              type="button"
                              className="btn-row btn-row--danger"
                              onClick={() => handleExcluirDocumento(doc)}
                              disabled={isExcluindo}
                            >
                              Excluir
                            </button>
                          ) : null}
                        </div>
                      ) : (
                        <span>—</span>
                      )}
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>

      {modal.type === 'importar' ? (
        <ImportarNfeModal
          fornecedores={fornecedores}
          isSaving={isSaving}
          feedback={modalFeedback}
          onClose={closeModal}
          onSubmit={handleImportarXml}
        />
      ) : null}

      {modal.type === 'recibo' ? (
        <ReciboSimplesFormModal
          formData={reciboForm}
          produtos={produtos}
          isSaving={isSaving}
          feedback={modalFeedback}
          onClose={closeModal}
          onChange={(e) => setReciboForm((c) => ({ ...c, [e.target.name]: e.target.value }))}
          onCriarProduto={abrirCriarProduto}
          onSubmit={handleCadastrarRecibo}
        />
      ) : null}

      {criarProdutoModalOpen ? (
        <InsumoEstoqueFormModal
          mode="create"
          formData={novoProdutoForm}
          unidades={unidades}
          grupos={grupos}
          isSaving={isSavingProduto}
          feedback={novoProdutoFeedback}
          onClose={() => setCriarProdutoModalOpen(false)}
          onChange={(e) => setNovoProdutoForm((c) => ({ ...c, [e.target.name]: e.target.value }))}
          onSubmit={handleSubmitNovoProduto}
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

      {vendaModalOpen ? (
        <RegistrarVendaModal
          lotes={lotes}
          animaisDisponiveis={animaisDisponiveis}
          compradores={compradores}
          isSaving={isSavingVenda}
          feedback={vendaFeedback}
          onClose={() => setVendaModalOpen(false)}
          onSubmitLeite={handleSubmitVendaLeite}
          onSubmitAnimal={handleSubmitVendaAnimal}
        />
      ) : null}
    </>
  )
}

export default NotasFiscaisTab
