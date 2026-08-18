import { request } from '../../../integration/apiClient'

function sanitize(value) {
  return String(value ?? '').trim()
}

function normalizeGrupoProduto(raw) {
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    codigoPrefixo: raw?.codigoPrefixo ?? '',
  }
}

function toPayload(formData) {
  const nome = sanitize(formData.nome)
  if (!nome) throw new Error('O nome do grupo é obrigatório.')

  const codigoPrefixo = sanitize(formData.codigoPrefixo)
  if (!/^\d{2}$/.test(codigoPrefixo)) {
    throw new Error('O prefixo deve conter exatamente 2 dígitos numéricos.')
  }

  return { nome, codigoPrefixo }
}

async function listarGruposProduto(termo) {
  const limpo = sanitize(termo)
  const query = limpo ? `?busca=${encodeURIComponent(limpo)}` : ''
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

export {
  atualizarGrupoProduto,
  cadastrarGrupoProduto,
  deletarGrupoProduto,
  listarGruposProduto,
  normalizeGrupoProduto,
}
