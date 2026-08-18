import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitizeText(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeVacina(raw) {
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    pendente: raw?.pendente === true,
  }
}

function normalizeInsumoEstoque(raw) {
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    tipo: raw?.tipo ?? '',
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

async function listarVacinas(termo) {
  const limpo = sanitizeText(termo)
  const query = limpo ? `?busca=${encodeURIComponent(limpo)}` : ''
  const payload = await request(`/insumos/vacinas${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar vacinas.')
  }
  return payload.map(normalizeVacina)
}

async function cadastrarVacina({ nome, pendente = false }) {
  const nomeLimpo = sanitizeText(nome)
  if (!nomeLimpo) {
    throw new Error('Informe o nome da vacina.')
  }
  const payload = await request('/insumos/vacinas', {
    method: 'POST',
    body: JSON.stringify({ nome: nomeLimpo, pendente }),
  })
  return normalizeVacina(payload)
}

async function atualizarVacina(id, { nome, pendente }) {
  if (!id) {
    throw new Error('Vacina sem identificador.')
  }
  const body = {}
  if (typeof nome === 'string') {
    const nomeLimpo = sanitizeText(nome)
    if (!nomeLimpo) {
      throw new Error('Informe o nome da vacina.')
    }
    body.nome = nomeLimpo
  }
  if (typeof pendente === 'boolean') {
    body.pendente = pendente
  }
  const payload = await request(`/insumos/vacinas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  return normalizeVacina(payload)
}

function confirmarVacina(id) {
  return atualizarVacina(id, { pendente: false })
}

function deletarVacina(id) {
  if (!id) {
    throw new Error('Vacina sem identificador.')
  }
  return request(`/insumos/vacinas/${id}`, { method: 'DELETE' })
}

// ── Estoque ────────────────────────────────────────────────────────────

async function listarEstoque(termo) {
  const limpo = sanitizeText(termo)
  const query = limpo ? `?busca=${encodeURIComponent(limpo)}` : ''
  const payload = await request(`/insumos/estoque${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar o estoque.')
  }
  return payload.map(normalizeInsumoEstoque)
}

async function cadastrarInsumoEstoque(email, formData) {
  const body = {
    nome: sanitizeText(formData.nome),
    tipo: formData.tipo,
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
  atualizarVacina,
  cadastrarVacina,
  confirmarVacina,
  deletarVacina,
  listarVacinas,
  listarEstoque,
  cadastrarInsumoEstoque,
  atualizarInsumoEstoque,
  registrarEntradaEstoque,
}
