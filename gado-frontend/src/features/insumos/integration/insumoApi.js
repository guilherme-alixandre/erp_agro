import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

// Autenticação agora é feita via o token JWT anexado automaticamente pelo apiClient.
function usuarioHeaders() {
  return {}
}

function normalizeInsumoEstoque(raw) {
  const statusRaw = raw?.status
  const status = statusRaw === 'A' ? 'ATIVO' : statusRaw === 'I' ? 'INATIVO' : (statusRaw ?? 'ATIVO')
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    tipo: raw?.tipo ?? '',
    status,
    codigoProduto: raw?.codigoProduto ?? '',
    grupoProdutoId: raw?.grupoProdutoId ?? null,
    grupoProdutoNome: raw?.grupoProdutoNome ?? '',
    saldoAtual: raw?.saldoAtual ?? 0,
    estoqueMinimo: raw?.estoqueMinimo ?? null,
    abaixoDoEstoqueMinimo: raw?.abaixoDoEstoqueMinimo === true,
    unidadeMedidaPrimariaId: raw?.unidadeMedidaPrimariaId ?? null,
    unidadeMedidaPrimariaSigla: raw?.unidadeMedidaPrimariaSigla ?? '',
    unidadeMedidaSecundariaId: raw?.unidadeMedidaSecundariaId ?? null,
    unidadeMedidaSecundariaSigla: raw?.unidadeMedidaSecundariaSigla ?? '',
    fatorConversao: raw?.fatorConversao ?? null,
    precoCompraMedio: raw?.precoCompraMedio ?? null,
    precoUltimaCompra: raw?.precoUltimaCompra ?? null,
    numeroNf: raw?.numeroNf ?? '',
    chaveAcessoNf: raw?.chaveAcessoNf ?? '',
    parceiroId: raw?.parceiroId ?? null,
    parceiroNome: raw?.parceiroNome ?? '',
  }
}

// ── Catálogo geral de Insumos (Estoque) ──────────────────────────────────

async function listarEstoque(termo, statusFiltro = 'ATIVO') {
  const limpo = sanitizeText(termo)
  const params = new URLSearchParams()
  if (limpo) params.set('busca', limpo)
  if (statusFiltro && statusFiltro !== 'TODOS') {
    params.set('status', statusFiltro === 'ATIVO' ? 'A' : 'I')
  }
  const query = params.toString() ? `?${params.toString()}` : ''
  const payload = await request(`/insumos/estoque${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar o estoque.')
  }
  return payload.map(normalizeInsumoEstoque)
}

function inativarInsumoEstoque(id, email) {
  return request(`/insumos/estoque/${id}`, { method: 'DELETE', headers: usuarioHeaders(email) })
}

function reativarInsumoEstoque(id, email) {
  return request(`/insumos/estoque/${id}/reativar`, { method: 'PUT', headers: usuarioHeaders(email) })
}

async function cadastrarInsumoEstoque(email, formData) {
  const body = {
    nome: sanitizeText(formData.nome),
    tipo: formData.tipo,
    grupoProdutoId: formData.grupoProdutoId ? Number(formData.grupoProdutoId) : null,
    unidadeMedidaPrimariaId: Number(formData.unidadeMedidaPrimariaId),
    unidadeMedidaSecundariaId: formData.unidadeMedidaSecundariaId
      ? Number(formData.unidadeMedidaSecundariaId)
      : null,
    fatorConversao: formData.fatorConversao ? Number(formData.fatorConversao) : null,
    estoqueMinimo: formData.estoqueMinimo !== '' ? Number(formData.estoqueMinimo) : null,
  }
  const payload = await request('/insumos/estoque', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeInsumoEstoque(payload)
}

async function atualizarInsumoEstoque(id, email, formData) {
  const body = {
    nome: sanitizeText(formData.nome) || undefined,
    unidadeMedidaSecundariaId: formData.unidadeMedidaSecundariaId
      ? Number(formData.unidadeMedidaSecundariaId)
      : undefined,
    fatorConversao: formData.fatorConversao ? Number(formData.fatorConversao) : undefined,
    estoqueMinimo: formData.estoqueMinimo !== '' ? Number(formData.estoqueMinimo) : undefined,
    precoCompraMedio: formData.precoCompraMedio !== '' ? Number(formData.precoCompraMedio) : undefined,
    precoUltimaCompra: formData.precoUltimaCompra !== '' ? Number(formData.precoUltimaCompra) : undefined,
  }
  const payload = await request(`/insumos/estoque/${id}`, {
    method: 'PUT',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeInsumoEstoque(payload)
}

async function registrarEntradaEstoque(id, email, formData) {
  const body = {
    quantidade: Number(formData.quantidade),
    precoUnitario: Number(formData.precoUnitario),
    numeroNf: sanitizeText(formData.numeroNf) || null,
    chaveAcessoNf: sanitizeText(formData.chaveAcessoNf) || null,
    dataEntrada: formData.dataEntrada || null,
  }
  const payload = await request(`/insumos/estoque/${id}/entradas`, {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeInsumoEstoque(payload)
}

export {
  listarEstoque,
  cadastrarInsumoEstoque,
  atualizarInsumoEstoque,
  registrarEntradaEstoque,
  inativarInsumoEstoque,
  reativarInsumoEstoque,
}
