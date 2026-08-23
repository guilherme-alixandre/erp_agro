import { request } from '../../../integration/apiClient'

// Autenticação agora é feita via o token JWT anexado automaticamente pelo apiClient.
function usuarioHeaders() {
  return {}
}

function normalizeNotaFiscal(raw) {
  return {
    id: raw?.id ?? null,
    direcao: raw?.direcao ?? 'ENTRADA',
    tipoDocumento: raw?.tipoDocumento ?? '',
    numeroDocumento: raw?.numeroDocumento ?? '',
    chaveAcesso: raw?.chaveAcesso ?? '',
    dataEmissao: raw?.dataEmissao ?? null,
    valorTotal: raw?.valorTotal ?? 0,
    status: raw?.status ?? '',
  }
}

async function listarNotasFiscais(email, filtros = {}) {
  const params = new URLSearchParams()
  if (filtros.numero) params.set('numero', filtros.numero)
  if (filtros.chave) params.set('chave', filtros.chave)
  if (filtros.direcao) params.set('direcao', filtros.direcao)
  const query = params.toString() ? `?${params.toString()}` : ''
  const payload = await request(`/notas-fiscais${query}`, { headers: usuarioHeaders(email) })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar as notas fiscais.')
  }
  return payload.map(normalizeNotaFiscal)
}

export { listarNotasFiscais }
