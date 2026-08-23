import { request } from '../../../integration/apiClient'

function normalizeMovimentacao(raw) {
  return {
    id: raw?.id ?? null,
    tipo: raw?.tipo ?? '',
    quantidade: raw?.quantidade ?? 0,
    valorUnitario: raw?.valorUnitario ?? 0,
    dataMovimentacao: raw?.dataMovimentacao ?? null,
    insumoId: raw?.insumoId ?? null,
    insumoNome: raw?.insumoNome ?? '',
    unidadeMedidaSigla: raw?.unidadeMedidaSigla ?? '',
    parceiroId: raw?.parceiroId ?? null,
    parceiroNome: raw?.parceiroNome ?? '',
    setorId: raw?.setorId ?? null,
    setorNome: raw?.setorNome ?? '',
    animalId: raw?.animalId ?? null,
    animalCodigoBrinco: raw?.animalCodigoBrinco ?? '',
  }
}

async function listarMovimentacoes(insumoId) {
  const query = insumoId ? `?insumoId=${encodeURIComponent(insumoId)}` : ''
  const payload = await request(`/movimentacaoEstoque${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar as movimentações de estoque.')
  }
  return payload.map(normalizeMovimentacao)
}

const TIPO_LABEL = {
  ENTRADA: 'Entrada',
  SAIDA: 'Saída',
  APLICACAO: 'Aplicação',
  PERDA: 'Perda',
}

export { listarMovimentacoes, TIPO_LABEL }
