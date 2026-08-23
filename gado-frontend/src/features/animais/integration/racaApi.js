import { request } from '../../../integration/apiClient'

function sanitize(value) {
  return String(value ?? '').trim()
}

// Autenticação agora é feita via o token JWT anexado automaticamente pelo apiClient.
function usuarioHeaders() {
  return {}
}

function normalizeRaca(raw) {
  const statusRaw = raw?.status
  const status = statusRaw === 'A' ? 'ATIVO' : statusRaw === 'I' ? 'INATIVO' : (statusRaw ?? 'ATIVO')
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    sigla: raw?.sigla ?? '',
    status,
    produtoId: raw?.produtoId ?? null,
    produtoNome: raw?.produtoNome ?? '',
    produtoCodigo: raw?.produtoCodigo ?? '',
    saldoAtual: raw?.saldoAtual ?? 0,
    precoCompraMedio: raw?.precoCompraMedio ?? null,
  }
}

function toPayload(formData) {
  const nome = sanitize(formData.nome)
  if (!nome) throw new Error('O nome da raça é obrigatório.')

  const sigla = sanitize(formData.sigla).toUpperCase()
  if (!/^[A-Z]{2,4}$/.test(sigla)) {
    throw new Error('A sigla deve ter de 2 a 4 letras.')
  }

  return { nome, sigla }
}

async function listarRacas(termo, statusFiltro) {
  const limpo = sanitize(termo)
  const params = new URLSearchParams()
  if (limpo) params.set('busca', limpo)
  if (statusFiltro && statusFiltro !== 'TODOS') {
    params.set('status', statusFiltro === 'ATIVO' ? 'A' : 'I')
  }
  const query = params.toString() ? `?${params.toString()}` : ''
  const payload = await request(`/racas${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar raças.')
  }
  return payload.map(normalizeRaca)
}

async function cadastrarRaca(email, formData) {
  const payload = await request('/racas', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(toPayload(formData)),
  })
  return normalizeRaca(payload)
}

async function atualizarRaca(id, email, formData) {
  const payload = await request(`/racas/${id}`, {
    method: 'PUT',
    headers: usuarioHeaders(email),
    body: JSON.stringify(toPayload(formData)),
  })
  return normalizeRaca(payload)
}

function deletarRaca(id, email) {
  return request(`/racas/${id}`, { method: 'DELETE', headers: usuarioHeaders(email) })
}

function reativarRaca(id, email) {
  return request(`/racas/${id}/reativar`, { method: 'PUT', headers: usuarioHeaders(email) })
}

export {
  atualizarRaca,
  cadastrarRaca,
  deletarRaca,
  reativarRaca,
  listarRacas,
  normalizeRaca,
}
