import { request } from '../../../integration/apiClient'

function sanitize(value) {
  return String(value ?? '').trim()
}

function normalizeGrupoProduto(raw) {
  const statusRaw = raw?.status
  const status = statusRaw === 'A' ? 'ATIVO' : statusRaw === 'I' ? 'INATIVO' : (statusRaw ?? 'ATIVO')
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    codigoPrefixo: raw?.codigoPrefixo ?? '',
    naturezaFinanceira: raw?.naturezaFinanceira ?? '',
    status,
  }
}

function toPayload(formData) {
  const nome = sanitize(formData.nome)
  if (!nome) throw new Error('O nome do grupo é obrigatório.')

  const codigoPrefixo = sanitize(formData.codigoPrefixo)
  if (!/^\d{2}$/.test(codigoPrefixo)) {
    throw new Error('O prefixo deve conter exatamente 2 dígitos numéricos.')
  }

  const naturezaFinanceira = sanitize(formData.naturezaFinanceira)
  if (!['CUSTO', 'GASTO'].includes(naturezaFinanceira)) {
    throw new Error('Selecione a natureza financeira do grupo (Custo ou Gasto).')
  }

  return { nome, codigoPrefixo, naturezaFinanceira }
}

async function listarGruposProduto(termo, statusFiltro) {
  const limpo = sanitize(termo)
  const params = new URLSearchParams()
  if (limpo) params.set('busca', limpo)
  if (statusFiltro && statusFiltro !== 'TODOS') {
    params.set('status', statusFiltro === 'ATIVO' ? 'A' : 'I')
  }
  const query = params.toString() ? `?${params.toString()}` : ''
  const payload = await request(`/grupos-produto${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar grupos de produto.')
  }
  return payload.map(normalizeGrupoProduto)
}

async function cadastrarGrupoProduto(formData) {
  const payload = await request('/grupos-produto', {
    method: 'POST',
    body: JSON.stringify(toPayload(formData)),
  })
  return normalizeGrupoProduto(payload)
}

async function atualizarGrupoProduto(id, formData) {
  const payload = await request(`/grupos-produto/${id}`, {
    method: 'PUT',
    body: JSON.stringify(toPayload(formData)),
  })
  return normalizeGrupoProduto(payload)
}

function deletarGrupoProduto(id) {
  return request(`/grupos-produto/${id}`, { method: 'DELETE' })
}

function reativarGrupoProduto(id) {
  return request(`/grupos-produto/${id}/reativar`, { method: 'PUT' })
}

export {
  atualizarGrupoProduto,
  cadastrarGrupoProduto,
  deletarGrupoProduto,
  reativarGrupoProduto,
  listarGruposProduto,
  normalizeGrupoProduto,
}
