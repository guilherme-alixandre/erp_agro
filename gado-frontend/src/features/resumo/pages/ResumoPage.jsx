import { useCallback, useEffect, useState } from 'react'
import { buscarResumo } from '../integration/resumoApi'
import ModuleHeader from '../../../components/shared/ModuleHeader'
import { formatarData, formatarMoeda } from '../../../utils/formatters'
import '../../animais/styles/animais.css'
import '../styles/resumo.css'

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

  const lucroLiquido = resumo?.lucroLiquido ?? 0
  const lucroPositivo = lucroLiquido >= 0

  // O backend só envia os totais do rebanho para quem vê o financeiro (Administrador e Gerente).
  const plural = (quantidade, singular, varios) => (quantidade === 1 ? singular : varios)
  const metricas = resumo && !resumo.financeiroVisivel
    ? [
        { value: resumo.alertasEstoque.length, label: plural(resumo.alertasEstoque.length, 'Alerta', 'Alertas') },
        { value: resumo.tarefasPendentes.length, label: plural(resumo.tarefasPendentes.length, 'Tarefa', 'Tarefas') },
      ]
    : [
        { value: resumo?.totalAnimais ?? '—', label: plural(resumo?.totalAnimais, 'Animal', 'Animais') },
        { value: resumo?.totalLotes ?? '—', label: plural(resumo?.totalLotes, 'Lote', 'Lotes') },
        { value: resumo?.tarefasPendentes?.length ?? '—', label: plural(resumo?.tarefasPendentes?.length, 'Tarefa', 'Tarefas') },
      ]

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
        <ModuleHeader
          icon="home"
          eyebrow={`Olá, ${currentUser.nome?.split(' ')[0] || 'produtor'}`}
          title="Resumo da fazenda"
          description="O que precisa da sua atenção hoje, reunido em uma visão simples."
          metrics={metricas}
        />

        {feedback.message ? (
          <p className={`feedback ${feedback.type === 'error' ? 'feedback--error' : 'feedback--info'}`}>
            {feedback.message}
          </p>
        ) : null}

        {isLoading ? (
          <p className="animals-count">Carregando...</p>
        ) : !resumo ? (
          <div className="resumo-empty-state">
            <strong>Não foi possível montar o resumo agora.</strong>
            <span>Confira o servidor e tente carregar os indicadores novamente.</span>
            <button type="button" className="btn-primary" onClick={fetchResumo}>
              Tentar novamente
            </button>
          </div>
        ) : (
          <>
            {resumo.financeiroVisivel ? (
              <>
                <div className="resumo-grid">
                  <button type="button" className="resumo-card" onClick={() => onNavigate('animais')}>
                    <p className="resumo-card__label">Quantidade de animais</p>
                    <p className="resumo-card__value">{resumo.totalAnimais}</p>
                  </button>
                  <button type="button" className="resumo-card" onClick={() => onNavigate('lotes')}>
                    <p className="resumo-card__label">Quantidade de lotes</p>
                    <p className="resumo-card__value">{resumo.totalLotes}</p>
                  </button>
                  <button type="button" className="resumo-card" onClick={() => onNavigate('setores')}>
                    <p className="resumo-card__label">Quantidade de setores</p>
                    <p className="resumo-card__value">{resumo.totalSetores}</p>
                  </button>
                </div>

                <div className="resumo-grid">
                  <button type="button" className="resumo-card" onClick={() => onNavigate('financeiro')}>
                    <p className="resumo-card__label">Vendas do mês</p>
                    <p className="resumo-card__value resumo-card__value--positive">
                      + {formatarMoeda(resumo.vendasMensais)}
                    </p>
                    {resumo.maiorFonteReceitaLabel || resumo.menorFonteReceitaLabel ? (
                      <span className="resumo-card__tags">
                        {resumo.maiorFonteReceitaLabel ? (
                          <span className="resumo-card__tag">Maior fonte: {resumo.maiorFonteReceitaLabel}</span>
                        ) : null}
                        {resumo.menorFonteReceitaLabel && resumo.menorFonteReceitaLabel !== resumo.maiorFonteReceitaLabel ? (
                          <span className="resumo-card__tag">Menor fonte: {resumo.menorFonteReceitaLabel}</span>
                        ) : null}
                      </span>
                    ) : null}
                  </button>
                  <button type="button" className="resumo-card" onClick={() => onNavigate('financeiro')}>
                    <p className="resumo-card__label">Gastos do mês</p>
                    <p className="resumo-card__value resumo-card__value--negative">
                      - {formatarMoeda(resumo.gastosMensais)}
                    </p>
                  </button>
                </div>

                <button
                  type="button"
                  className={`resumo-lucro-bar ${lucroPositivo ? 'resumo-lucro-bar--positivo' : 'resumo-lucro-bar--negativo'}`}
                  onClick={() => onNavigate('financeiro')}
                >
                  {lucroPositivo
                    ? `Lucro líquido de ${formatarMoeda(lucroLiquido)} este mês`
                    : `Prejuízo de ${formatarMoeda(Math.abs(lucroLiquido))} este mês`}
                </button>
              </>
            ) : null}

            <div className="resumo-section">
              <h3>Alertas de estoque</h3>
              {resumo.alertasEstoque.length === 0 ? (
                <p className="animals-count">Nenhum produto abaixo do estoque mínimo.</p>
              ) : (
                resumo.alertasEstoque.map((a) => (
                  <div className="resumo-alerta-row" key={a.insumoId}>
                    <span>
                      <strong>{a.insumoNome}</strong> abaixo do mínimo: {a.saldoAtual} {a.unidadeMedidaSigla}{' '}
                      (mínimo: {a.estoqueMinimo} {a.unidadeMedidaSigla})
                    </span>
                    <button type="button" className="btn-row" onClick={() => onNavigate('insumos')}>
                      Ir ao estoque
                    </button>
                  </div>
                ))
              )}
            </div>

            <div className="resumo-section">
              <h3>Tarefas pendentes</h3>
              {resumo.tarefasPendentes.length === 0 ? (
                <p className="animals-count">Nenhuma tarefa pendente.</p>
              ) : (
                resumo.tarefasPendentes.map((t) => (
                  <button type="button" className="resumo-tarefa-row" key={t.id} onClick={() => onNavigate('tarefas')}>
                    <strong>{t.descricao}</strong>
                    <span className="resumo-tarefa-row__prazo">Prazo: {formatarData(t.dataLimite)}</span>
                  </button>
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
