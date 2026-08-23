import { request } from '../../../integration/apiClient'

function sanitize(value) {
  return String(value ?? '').trim()
}

function normalizeUnidade(raw) {
  const statusRaw = raw?.status
  const status = statusRaw === 'A' ? 'ATIVO' : statusRaw === 'I' ? 'INATIVO' : (statusRaw ?? 'ATIVO')
  return {
    id: raw?.id ?? null,
    unidade: raw?.unidade ?? '',
    status,
  }
}

function toPayload(formData) {
  const unidade = sanitize(formData.unidade)
  if (!unidade) throw new Error('A unidade é obrigatória.')
  return { unidade }
}

async function listarUnidadesMedida(termo = '', statusFiltro = 'ATIVO') {
  const limpo = sanitize(termo)
  const params = new URLSearchParams()
  if (limpo) params.set('busca', limpo)
  if (statusFiltro && statusFiltro !== 'TODOS') {
    params.set('status', statusFiltro === 'ATIVO' ? 'A' : 'I')
  }
  const query = params.toString() ? `?${params.toString()}` : ''
  const payload = await request(`/unidades-medida${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar unidades de medida.')
  }
  return payload.map(normalizeUnidade)
}

async function cadastrarUnidadeMedida(formData) {
  const payload = await request('/unidades-medida', {
    method: 'POST',
    body: JSON.stringify(toPayload(formData)),
  })
  return normalizeUnidade(payload)
}

async function atualizarUnidadeMedida(id, formData) {
  const payload = await request(`/unidades-medida/${id}`, {
    method: 'PUT',
    body: JSON.stringify(toPayload(formData)),
  })
  return normalizeUnidade(payload)
}

function deletarUnidadeMedida(id) {
  return request(`/unidades-medida/${id}`, { method: 'DELETE' })
}

function reativarUnidadeMedida(id) {
  return request(`/unidades-medida/${id}/reativar`, { method: 'PUT' })
}

export {
  listarUnidadesMedida,
  cadastrarUnidadeMedida,
  atualizarUnidadeMedida,
  deletarUnidadeMedida,
  reativarUnidadeMedida,
  normalizeUnidade,
}
