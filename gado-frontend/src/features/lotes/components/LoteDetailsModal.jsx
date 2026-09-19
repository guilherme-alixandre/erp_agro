import { useEffect, useState } from 'react'
import { buscarCustoRacaoLote, buscarPerdasAlimentacaoLote } from '../integration/loteApi'
import { formatarData, formatarMoeda } from '../../../utils/formatters'

function LoteDetailsModal({ lote, onClose, onEdit, onDelete, isDeleting, canEdit = true, canDelete = true }) {
  const [custoRacao, setCustoRacao] = useState(null)
  const [perdasAlimentacao, setPerdasAlimentacao] = useState(null)

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
    return () => {
      cancelado = true
    }
  }, [lote.id])

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
                  <ul className="lote-alocacao__animais">
                    {aloc.animais.map((a) => (
                      <li key={a.id}>
                        <strong>{a.codigoBrinco}</strong>
                        {a.racaNome ? <span> — {a.racaNome}</span> : null}
                      </li>
                    ))}
                  </ul>
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
