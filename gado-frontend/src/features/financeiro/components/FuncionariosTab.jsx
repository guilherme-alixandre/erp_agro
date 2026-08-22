import { useCallback, useEffect, useState } from 'react'
import {
  atualizarFuncionario,
  cadastrarFuncionario,
  estornarPagamento,
  lancarPagamento,
  listarFuncionarios,
  listarPagamentosPorBloco,
} from '../integration/folhaPagamentoApi'
import FuncionarioFormModal from './FuncionarioFormModal'
import LancarPagamentoModal from './LancarPagamentoModal'
import EstornarPagamentoModal from './EstornarPagamentoModal'

const PERFIS_GERENCIAIS = ['ADMINISTRADOR', 'GERENTE']

const defaultFuncionarioForm = {
  nomeCompleto: '', cpf: '', cargo: '', dataAdmissao: '', dataDemissao: '',
  salarioBase: '', percentualInss: '', percentualFgts: '',
  valorValeTransporte: '', valorValeAlimentacao: '', valorPlanoSaude: '',
}

const STATUS_LABEL = { PAGO: 'Pago', PENDENTE: 'Pendente', ATRASADO: 'Atrasado' }

const CARGO_LABEL = {
  GERENTE: 'Gerente',
  CUIDADOR: 'Cuidador',
  CUIDADOR_CHEFE: 'Cuidador Chefe',
  ADMINISTRADOR: 'Administrador',
  FINANCEIRO: 'Financeiro',
}

const agora = new Date()

function defaultPagamentoForm() {
  return {
    anoReferencia: agora.getFullYear(),
    mesReferencia: agora.getMonth() + 1,
    dataPagamento: '',
    descontoOutros: '',
    valorBonus: '',
  }
}

function FuncionariosTab({ currentUser }) {
  const isGerencial = PERFIS_GERENCIAIS.includes(currentUser?.perfil)

  const [funcionarios, setFuncionarios] = useState([])
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const [funcionarioModalOpen, setFuncionarioModalOpen] = useState(false)
  const [funcionarioFormMode, setFuncionarioFormMode] = useState('create')
  const [funcionarioEditandoId, setFuncionarioEditandoId] = useState(null)
  const [funcionarioForm, setFuncionarioForm] = useState(defaultFuncionarioForm)
  const [isSavingFuncionario, setIsSavingFuncionario] = useState(false)
  const [funcionarioFeedback, setFuncionarioFeedback] = useState('')

  const [pagamentoAlvo, setPagamentoAlvo] = useState(null)
  const [pagamentoForm, setPagamentoForm] = useState(defaultPagamentoForm())
  const [isSavingPagamento, setIsSavingPagamento] = useState(false)
  const [pagamentoFeedback, setPagamentoFeedback] = useState('')

  const [pagamentoParaEstornar, setPagamentoParaEstornar] = useState(null)
  const [motivoEstorno, setMotivoEstorno] = useState('')
  const [isEstornando, setIsEstornando] = useState(false)
  const [estornoFeedback, setEstornoFeedback] = useState('')

  const [bloco, setBloco] = useState({ ano: agora.getFullYear(), mes: agora.getMonth() + 1 })
  const [pagamentosDoMes, setPagamentosDoMes] = useState([])
  const [isLoadingPagamentos, setIsLoadingPagamentos] = useState(false)

  const fetchFuncionarios = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const lista = await listarFuncionarios(currentUser.email)
      setFuncionarios(lista)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar os funcionários.' })
    } finally {
      setIsLoading(false)
    }
  }, [currentUser.email])

  const fetchPagamentosDoMes = useCallback(async () => {
    setIsLoadingPagamentos(true)
    try {
      const lista = await listarPagamentosPorBloco(currentUser.email, bloco.ano, bloco.mes)
      setPagamentosDoMes(lista)
    } catch {
      setPagamentosDoMes([])
    } finally {
      setIsLoadingPagamentos(false)
    }
  }, [currentUser.email, bloco])

  useEffect(() => {
    fetchFuncionarios()
  }, [fetchFuncionarios])

  useEffect(() => {
    fetchPagamentosDoMes()
  }, [fetchPagamentosDoMes])

  function openFuncionarioModal() {
    setFuncionarioFormMode('create')
    setFuncionarioEditandoId(null)
    setFuncionarioForm(defaultFuncionarioForm)
    setFuncionarioFeedback('')
    setFuncionarioModalOpen(true)
  }

  function openEditFuncionarioModal(f) {
    setFuncionarioFormMode('edit')
    setFuncionarioEditandoId(f.id)
    setFuncionarioFeedback('')
    setFuncionarioForm({
      nomeCompleto: f.nomeCompleto,
      cpf: f.cpf,
      cargo: f.cargo,
      dataAdmissao: f.dataAdmissao ?? '',
      dataDemissao: f.dataDemissao ?? '',
      salarioBase: f.salarioBase,
      percentualInss: f.percentualInss,
      percentualFgts: f.percentualFgts,
      valorValeTransporte: f.valorValeTransporte ?? '',
      valorValeAlimentacao: f.valorValeAlimentacao ?? '',
      valorPlanoSaude: f.valorPlanoSaude ?? '',
    })
    setFuncionarioModalOpen(true)
  }

  async function handleSubmitFuncionario(event) {
    event.preventDefault()
    setIsSavingFuncionario(true)
    setFuncionarioFeedback('')
    try {
      if (funcionarioFormMode === 'edit') {
        await atualizarFuncionario(funcionarioEditandoId, currentUser.email, funcionarioForm)
        setFeedback({ type: 'info', message: 'Funcionário atualizado com sucesso.' })
      } else {
        await cadastrarFuncionario(currentUser.email, funcionarioForm)
        setFeedback({ type: 'info', message: 'Funcionário cadastrado com sucesso.' })
      }
      setFuncionarioModalOpen(false)
      await fetchFuncionarios()
    } catch (error) {
      setFuncionarioFeedback(error.message || 'Falha ao salvar o funcionário.')
    } finally {
      setIsSavingFuncionario(false)
    }
  }

  function abrirPagamento(funcionario) {
    setPagamentoAlvo(funcionario)
    setPagamentoForm(defaultPagamentoForm())
    setPagamentoFeedback('')
  }

  async function handleSubmitPagamento(event) {
    event.preventDefault()
    setIsSavingPagamento(true)
    setPagamentoFeedback('')
    try {
      await lancarPagamento(currentUser.email, { ...pagamentoForm, funcionarioId: pagamentoAlvo.id })
      setFeedback({ type: 'info', message: 'Pagamento lançado com sucesso.' })
      setPagamentoAlvo(null)
      await fetchPagamentosDoMes()
    } catch (error) {
      setPagamentoFeedback(error.message || 'Falha ao lançar o pagamento.')
    } finally {
      setIsSavingPagamento(false)
    }
  }

  function abrirEstorno(pagamento) {
    setPagamentoParaEstornar(pagamento)
    setMotivoEstorno('')
    setEstornoFeedback('')
  }

  async function handleSubmitEstorno(event) {
    event.preventDefault()
    setIsEstornando(true)
    setEstornoFeedback('')
    try {
      await estornarPagamento(pagamentoParaEstornar.id, currentUser.email, motivoEstorno)
      setPagamentoParaEstornar(null)
      setFeedback({ type: 'info', message: 'Pagamento extornado com sucesso.' })
      await fetchPagamentosDoMes()
    } catch (error) {
      setEstornoFeedback(error.message || 'Falha ao extornar o pagamento.')
    } finally {
      setIsEstornando(false)
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
          {isLoading ? 'Carregando...' : `${funcionarios.length} funcionário(s)`}
        </p>
        {isGerencial ? (
          <button type="button" className="btn-new-entity" onClick={openFuncionarioModal}>
            + Novo Funcionário
          </button>
        ) : null}
      </div>

      <div className="data-table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Nome</th>
              <th>CPF</th>
              <th>Cargo</th>
              <th>Salário Base</th>
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr>
                <td colSpan={5} className="table-loading">Carregando...</td>
              </tr>
            ) : funcionarios.length === 0 ? (
              <tr>
                <td colSpan={5} className="table-empty">Nenhum funcionário cadastrado.</td>
              </tr>
            ) : (
              funcionarios.map((f) => (
                <tr key={f.id}>
                  <td>{f.nomeCompleto}</td>
                  <td>{f.cpf}</td>
                  <td>{CARGO_LABEL[f.cargo] ?? f.cargo}</td>
                  <td>R$ {Number(f.salarioBase ?? 0).toFixed(2)}</td>
                  <td>
                    {isGerencial ? (
                      <div className="row-actions">
                        <button type="button" className="btn-row" onClick={() => abrirPagamento(f)}>
                          Lançar Pagamento
                        </button>
                        <button type="button" className="btn-row btn-row--edit" onClick={() => openEditFuncionarioModal(f)}>
                          Editar
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

      <div className="financeiro-subsection">
        <div className="data-toolbar">
          <h3>Pagamentos do mês</h3>
          <div className="financeiro-bloco-selector">
            <select value={bloco.mes} onChange={(e) => setBloco((c) => ({ ...c, mes: Number(e.target.value) }))}>
              {Array.from({ length: 12 }, (_, i) => i + 1).map((mes) => (
                <option key={mes} value={mes}>{String(mes).padStart(2, '0')}</option>
              ))}
            </select>
            <input
              type="number"
              value={bloco.ano}
              onChange={(e) => setBloco((c) => ({ ...c, ano: Number(e.target.value) }))}
              style={{ width: '90px' }}
            />
          </div>
        </div>

        <div className="data-table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Funcionário</th>
                <th>Bruto</th>
                <th>Desconto INSS</th>
                <th>Bônus</th>
                <th>Líquido</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {isLoadingPagamentos ? (
                <tr>
                  <td colSpan={7} className="table-loading">Carregando...</td>
                </tr>
              ) : pagamentosDoMes.length === 0 ? (
                <tr>
                  <td colSpan={7} className="table-empty">Nenhum pagamento lançado neste mês.</td>
                </tr>
              ) : (
                pagamentosDoMes.map((p) => (
                  <tr key={p.id}>
                    <td>{p.funcionarioNome}</td>
                    <td>R$ {Number(p.valorBruto ?? 0).toFixed(2)}</td>
                    <td>R$ {Number(p.descontoInss ?? 0).toFixed(2)}</td>
                    <td>R$ {Number(p.valorBonus ?? 0).toFixed(2)}</td>
                    <td>R$ {Number(p.valorLiquido ?? 0).toFixed(2)}</td>
                    <td>
                      {p.estornado ? (
                        <span className="consumo-estoque__status--cancelado">Extornado</span>
                      ) : (
                        STATUS_LABEL[p.statusPagamento] ?? p.statusPagamento
                      )}
                    </td>
                    <td>
                      {isGerencial && !p.estornado ? (
                        <button type="button" className="btn-row btn-row--danger" onClick={() => abrirEstorno(p)}>
                          Extornar
                        </button>
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
      </div>

      {funcionarioModalOpen ? (
        <FuncionarioFormModal
          mode={funcionarioFormMode}
          formData={funcionarioForm}
          isSaving={isSavingFuncionario}
          feedback={funcionarioFeedback}
          onClose={() => setFuncionarioModalOpen(false)}
          onChange={(e) => setFuncionarioForm((c) => ({ ...c, [e.target.name]: e.target.value }))}
          onSubmit={handleSubmitFuncionario}
        />
      ) : null}

      {pagamentoAlvo ? (
        <LancarPagamentoModal
          funcionario={pagamentoAlvo}
          formData={pagamentoForm}
          isSaving={isSavingPagamento}
          feedback={pagamentoFeedback}
          onClose={() => setPagamentoAlvo(null)}
          onChange={(e) => setPagamentoForm((c) => ({ ...c, [e.target.name]: e.target.value }))}
          onSubmit={handleSubmitPagamento}
        />
      ) : null}

      {pagamentoParaEstornar ? (
        <EstornarPagamentoModal
          pagamento={pagamentoParaEstornar}
          motivoEstorno={motivoEstorno}
          isSaving={isEstornando}
          feedback={estornoFeedback}
          onClose={() => setPagamentoParaEstornar(null)}
          onChange={(e) => setMotivoEstorno(e.target.value)}
          onSubmit={handleSubmitEstorno}
        />
      ) : null}
    </>
  )
}

export default FuncionariosTab
