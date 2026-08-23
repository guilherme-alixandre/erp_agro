import { useCallback, useEffect, useState } from 'react'
import { buscarResumo } from '../integration/resumoApi'
import '../../animais/styles/animais.css'
import '../styles/resumo.css'

function formatarMoeda(valor) {
  return Number(valor ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatarData(dateText) {
  if (!dateText) return '—'
  const data = new Date(dateText)
  if (Number.isNaN(data.getTime())) return dateText
  return data.toLocaleDateString('pt-BR')
}

function ResumoPage({ currentUser, onLogout, onNavigate }) {
  const [resumo, setResumo] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  const fetchResumo = useCallback(async () => {
    setIsLoading(true)
    setFeedback({ type: '', message: '' })
    try {
      const dados = await buscarResumo()
      setResumo(dados)
    } catch (error) {
      setFeedback({ type: 'error', message: error.message || 'Falha ao carregar o resumo.' })
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchResumo()
  }, [fetchResumo])

  const lucroPositivo = (resumo?.lucroLiquido ?? 0) >= 0

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo"><img src="/logo.png" alt="GADO" /></div>
        <nav>
          <button type="button" className="menu-item menu-item--active">
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
          <button type="button" className="menu-item" onClick={() => onNavigate('tarefas')}>
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
          <h1>Resumo</h1>
          <span>Sessão ativa: {currentUser.nome}</span>
        </header>

        {feedback.message ? (
          <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
            {feedback.message}
          </p>
        ) : null}

        {isLoading || !resumo ? (
          <p className="animals-count">Carregando...</p>
        ) : (
          <>
            {resumo.financeiroVisivel ? (
              <>
                <div className="resumo-grid">
                  <button type="button" className="resumo-card" onClick={() => onNavigate('animais')}>
                    <p className="resumo-card__label">Quantidade de Animais</p>
                    <p className="resumo-card__value">{resumo.totalAnimais}</p>
                  </button>
                  <button type="button" className="resumo-card" onClick={() => onNavigate('lotes')}>
                    <p className="resumo-card__label">Quantidade de Lotes</p>
                    <p className="resumo-card__value">{resumo.totalLotes}</p>
                  </button>
                  <button type="button" className="resumo-card" onClick={() => onNavigate('setores')}>
                    <p className="resumo-card__label">Quantidade de Setores</p>
                    <p className="resumo-card__value">{resumo.totalSetores}</p>
                  </button>
                </div>

                <div className="resumo-grid">
                  <button type="button" className="resumo-card" onClick={() => onNavigate('financeiro')}>
                    <p className="resumo-card__label">Vendas Mensais</p>
                    <p className="resumo-card__value resumo-card__value--positive">
                      + {formatarMoeda(resumo.vendasMensais)}
                    </p>
                    {resumo.maiorFonteReceitaLabel ? (
                      <span className="resumo-card__tag">Maior Fonte de Renda: {resumo.maiorFonteReceitaLabel}</span>
                    ) : null}
                  </button>
                  <button type="button" className="resumo-card" onClick={() => onNavigate('financeiro')}>
                    <p className="resumo-card__label">Gastos Mensais</p>
                    <p className="resumo-card__value resumo-card__value--negative">
                      - {formatarMoeda(resumo.gastosMensais)}
                    </p>
                    {resumo.menorFonteReceitaLabel ? (
                      <span className="resumo-card__tag">Menor Fonte de Renda: {resumo.menorFonteReceitaLabel}</span>
                    ) : null}
                  </button>
                </div>

                <button
                  type="button"
                  className={`resumo-lucro-bar ${lucroPositivo ? 'resumo-lucro-bar--positivo' : 'resumo-lucro-bar--negativo'}`}
                  onClick={() => onNavigate('financeiro')}
                >
                  {lucroPositivo ? '+' : '-'} {formatarMoeda(Math.abs(resumo.lucroLiquido ?? 0))} de lucro líquido este mês
                </button>
              </>
            ) : null}

            <div className="resumo-section">
              <h3>Alerta de Estoque</h3>
              {resumo.alertasEstoque.length === 0 ? (
                <p className="animals-count">Nenhum produto abaixo do estoque mínimo.</p>
              ) : (
                resumo.alertasEstoque.map((a) => (
                  <div className="resumo-alerta-row" key={a.insumoId}>
                    <span>
                      Sem estoque suficiente de &quot;{a.insumoNome}&quot; — {a.saldoAtual} {a.unidadeMedidaSigla} (mínimo: {a.estoqueMinimo})
                    </span>
                    <button type="button" className="btn-row" onClick={() => onNavigate('insumos')}>
                      Ir ao estoque
                    </button>
                  </div>
                ))
              )}
            </div>

            <div className="resumo-section">
              <h3>Tarefas Diárias</h3>
              {resumo.tarefasPendentes.length === 0 ? (
                <p className="animals-count">Nenhuma tarefa pendente.</p>
              ) : (
                resumo.tarefasPendentes.map((t) => (
                  <div className="resumo-tarefa-row" key={t.id} onClick={() => onNavigate('tarefas')}>
                    <span>{t.descricao}</span>
                    <span>Prazo: {formatarData(t.dataLimite)}</span>
                  </div>
                ))
              )}
            </div>
          </>
        )}
      </section>
    </main>
  )
}

export default ResumoPage
