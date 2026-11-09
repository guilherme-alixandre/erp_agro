import { useEffect, useState } from 'react'
import { buscarCustoRacaoLote, buscarCustoTotalLote, buscarPerdasAlimentacaoLote } from '../integration/loteApi'
import { buscarAnimalPorBrinco } from '../../animais/integration/animalApi'
import { buscarCustoVidaAnimal } from '../../animais/integration/custoAnimalApi'
import { formatarData, formatarMoeda, formatCm } from '../../../utils/formatters'

const STATUS_ANIMAL_LABELS = {
  ATIVO: 'Ativo',
  OBSERVACAO: 'Em observação',
  VENDIDO: 'Vendido',
  OBITO: 'Óbito',
  ABATIDO: 'Abatido',
}

function LoteDetailsModal({ lote, onClose, onEdit, onDelete, isDeleting, canEdit = true, canDelete = true }) {
  const [custoRacao, setCustoRacao] = useState(null)
  const [perdasAlimentacao, setPerdasAlimentacao] = useState(null)
  const [custoTotal, setCustoTotal] = useState(null)

  const [expandedAnimalIds, setExpandedAnimalIds] = useState(() => new Set())
  const [animalDetalhes, setAnimalDetalhes] = useState({})

  useEffect(() => {
    let cancelado = false
    buscarCustoRacaoLote(lote.id)
      .then((dados) => {
        if (!cancelado) setCustoRacao(dados)
      })
      .catch(() => {
        if (!cancelado) setCustoRacao(null)
      })
    buscarPerdasAlimentacaoLote(lote.id)
      .then((dados) => {
        if (!cancelado) setPerdasAlimentacao(dados)
      })
      .catch(() => {
        if (!cancelado) setPerdasAlimentacao(null)
      })
    buscarCustoTotalLote(lote.id)
      .then((dados) => {
        if (!cancelado) setCustoTotal(dados)
      })
      .catch(() => {
        if (!cancelado) setCustoTotal(null)
      })
    return () => {
      cancelado = true
    }
  }, [lote.id])

  function carregarDetalheAnimal(animal) {
    setAnimalDetalhes((atual) => ({ ...atual, [animal.id]: { status: 'loading' } }))

    Promise.allSettled([
      buscarAnimalPorBrinco(animal.codigoBrinco),
      buscarCustoVidaAnimal(animal.id),
    ]).then(([animalResult, custoResult]) => {
      if (animalResult.status !== 'fulfilled') {
        setAnimalDetalhes((atual) => ({
          ...atual,
          [animal.id]: {
            status: 'error',
            error: animalResult.reason?.message || 'Falha ao buscar dados do animal.',
          },
        }))
        return
      }

      setAnimalDetalhes((atual) => ({
        ...atual,
        [animal.id]: {
          status: 'loaded',
          animal: animalResult.value,
          custoVida: custoResult.status === 'fulfilled' ? custoResult.value : null,
        },
      }))
    })
  }

  function toggleAnimal(animal) {
    setExpandedAnimalIds((atual) => {
      const next = new Set(atual)
      if (next.has(animal.id)) {
        next.delete(animal.id)
      } else {
        next.add(animal.id)
        if (!animalDetalhes[animal.id]) {
          carregarDetalheAnimal(animal)
        }
      }
      return next
    })
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>Detalhes do lote</h2>
          <button type="button" className="modal-close" aria-label="Fechar" onClick={onClose}>
            ✕
          </button>
        </div>

        <dl className="details-grid">
          <div>
            <dt>Código</dt>
            <dd>{lote.codigo}</dd>
          </div>
          <div>
            <dt>Status</dt>
            <dd>{lote.statusLote === 'ATIVO' ? 'Ativo' : 'Inativo'}</dd>
          </div>
          <div>
            <dt>Cor do brinco</dt>
            <dd>{lote.corBrinco || '-'}</dd>
          </div>
          <div>
            <dt>Raça predominante</dt>
            <dd>{lote.racaPredominante || '-'}</dd>
          </div>
          <div>
            <dt>Descrição</dt>
            <dd>{lote.descricao || '-'}</dd>
          </div>
          <div>
            <dt>Data de criação</dt>
            <dd>{formatarData(lote.dataCriacao)}</dd>
          </div>
          <div>
            <dt>Total de animais</dt>
            <dd>{lote.totalAnimais}</dd>
          </div>
          {custoRacao ? (
            <>
              <div>
                <dt>Custo de ração acumulado</dt>
                <dd>{formatarMoeda(custoRacao.custoTotalAcumulado)}</dd>
              </div>
              <div>
                <dt>Custo de ração por animal</dt>
                <dd>{formatarMoeda(custoRacao.custoPorAnimal)}</dd>
              </div>
            </>
          ) : null}
          {perdasAlimentacao && Number(perdasAlimentacao.valorTotalPerdido) > 0 ? (
            <>
              <div>
                <dt>Perdas de alimentação</dt>
                <dd>{formatarMoeda(perdasAlimentacao.valorTotalPerdido)}</dd>
              </div>
              <div>
                <dt>Perda de alimentação por animal</dt>
                <dd>{formatarMoeda(perdasAlimentacao.valorPerdidoPorAnimal)}</dd>
              </div>
            </>
          ) : null}
          <div>
            <dt>Criado por</dt>
            <dd>{lote.criadoPorNome || lote.criadoPorEmail || '-'}</dd>
          </div>
          {lote.alteradoPorNome || lote.alteradoPorEmail ? (
            <div>
              <dt>Alterado por</dt>
              <dd>{lote.alteradoPorNome || lote.alteradoPorEmail}</dd>
            </div>
          ) : null}
        </dl>

        {custoTotal && Number(custoTotal.totalGeral) > 0 ? (
          <>
            <h3 className="details-section">Custo total do lote</h3>
            <dl className="details-grid">
              <div>
                <dt>Aquisição</dt>
                <dd>{formatarMoeda(custoTotal.totalAquisicao)}</dd>
              </div>
              <div>
                <dt>Alimentação</dt>
                <dd>{formatarMoeda(custoTotal.totalAlimentacao)}</dd>
              </div>
              <div>
                <dt>Vacinação</dt>
                <dd>{formatarMoeda(custoTotal.totalVacinacao)}</dd>
              </div>
              <div>
                <dt>Total</dt>
                <dd>{formatarMoeda(custoTotal.totalGeral)}</dd>
              </div>
              <div>
                <dt>Total por animal</dt>
                <dd>{formatarMoeda(custoTotal.totalPorAnimal)}</dd>
              </div>
            </dl>
          </>
        ) : null}

        <h3 className="details-section">Divisão por setores</h3>

        {lote.alocacoes.length === 0 ? (
          <p className="vacinas-empty">Nenhum setor alocado.</p>
        ) : (
          <div className="lote-alocacoes">
            {lote.alocacoes.map((aloc) => (
              <div key={aloc.loteSectorId ?? aloc.setorId} className="lote-alocacao">
                <div className="lote-alocacao__header">
                  <strong>{aloc.setorNome}</strong>
                  <span className="lote-alocacao__capacidade">
                    {aloc.animais.length}/{aloc.capacidadeMaxima} animais
                  </span>
                </div>

                {aloc.animais.length === 0 ? (
                  <p className="vacinas-empty">Nenhum animal alocado neste setor.</p>
                ) : (
                  <div className="lote-alocacao__animais">
                    {aloc.animais.map((a) => {
                      const expandido = expandedAnimalIds.has(a.id)
                      const detalhe = animalDetalhes[a.id]
                      return (
                        <div key={a.id} className="animal-accordion-item">
                          <button
                            type="button"
                            className="animal-accordion-item__toggle"
                            onClick={() => toggleAnimal(a)}
                            aria-expanded={expandido}
                          >
                            <span className="animal-accordion-item__arrow" aria-hidden="true">
                              {expandido ? '▾' : '▸'}
                            </span>
                            <strong>{a.codigoBrinco}</strong>
                            {a.racaNome ? <span> — {a.racaNome}</span> : null}
                          </button>

                          {expandido ? (
                            <div className="animal-accordion-item__panel">
                              {!detalhe || detalhe.status === 'loading' ? (
                                <p className="vacinas-empty">Carregando...</p>
                              ) : detalhe.status === 'error' ? (
                                <p className="feedback feedback--error">{detalhe.error}</p>
                              ) : (
                                <>
                                  <dl className="details-grid details-grid--compact">
                                    <div>
                                      <dt>Brinco</dt>
                                      <dd>{detalhe.animal.codigoBrinco}</dd>
                                    </div>
                                    <div>
                                      <dt>Data de nascimento</dt>
                                      <dd>{formatarData(detalhe.animal.dataNascimento)}</dd>
                                    </div>
                                    <div>
                                      <dt>Peso</dt>
                                      <dd>{detalhe.animal.pesoAtual} kg</dd>
                                    </div>
                                    <div>
                                      <dt>Raça</dt>
                                      <dd>{detalhe.animal.racaNome || '-'}</dd>
                                    </div>
                                    <div>
                                      <dt>Cor</dt>
                                      <dd>{detalhe.animal.cor || '-'}</dd>
                                    </div>
                                    <div>
                                      <dt>Altura na cernelha</dt>
                                      <dd>{formatCm(detalhe.animal.alturaCernelha)}</dd>
                                    </div>
                                    <div>
                                      <dt>Perímetro torácico</dt>
                                      <dd>{formatCm(detalhe.animal.perimetroToracico)}</dd>
                                    </div>
                                    <div>
                                      <dt>Comprimento corporal</dt>
                                      <dd>{formatCm(detalhe.animal.comprimentoCorporal)}</dd>
                                    </div>
                                    <div>
                                      <dt>Sexo</dt>
                                      <dd>{detalhe.animal.sexo === 'F' ? 'Fêmea' : 'Macho'}</dd>
                                    </div>
                                    <div>
                                      <dt>Status</dt>
                                      <dd>
                                        {STATUS_ANIMAL_LABELS[detalhe.animal.statusAnimal] ?? detalhe.animal.statusAnimal}
                                      </dd>
                                    </div>
                                  </dl>

                                  {detalhe.custoVida ? (
                                    <dl className="details-grid details-grid--compact">
                                      <div>
                                        <dt>Custo de aquisição</dt>
                                        <dd>{formatarMoeda(detalhe.custoVida.totalAquisicao)}</dd>
                                      </div>
                                      <div>
                                        <dt>Custo de alimentação</dt>
                                        <dd>{formatarMoeda(detalhe.custoVida.totalAlimentacao)}</dd>
                                      </div>
                                      <div>
                                        <dt>Custo de vacinação</dt>
                                        <dd>{formatarMoeda(detalhe.custoVida.totalVacinacao)}</dd>
                                      </div>
                                      <div>
                                        <dt>Custo total</dt>
                                        <dd>{formatarMoeda(detalhe.custoVida.totalGeral)}</dd>
                                      </div>
                                    </dl>
                                  ) : (
                                    <p className="vacinas-empty">
                                      Não foi possível carregar o custo de vida deste animal.
                                    </p>
                                  )}
                                </>
                              )}
                            </div>
                          ) : null}
                        </div>
                      )
                    })}
                  </div>
                )}
              </div>
            ))}
          </div>
        )}

        {(canEdit || canDelete) ? (
          <div className="modal-actions">
            {canEdit ? (
              <button type="button" className="btn-secondary" onClick={onEdit}>
                Editar
              </button>
            ) : null}
            {canDelete ? (
              <button
                type="button"
                className="btn-danger"
                onClick={() => onDelete(lote)}
                disabled={isDeleting}
              >
                {isDeleting ? 'Excluindo...' : 'Excluir'}
              </button>
            ) : null}
          </div>
        ) : null}
      </div>
    </div>
  )
}

export default LoteDetailsModal
