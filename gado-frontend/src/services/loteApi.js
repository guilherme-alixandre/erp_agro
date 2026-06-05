import { request } from './apiClient'

// ── Normalização ──────────────────────────────────────────────────────────────

function normalizeAnimalResumo(raw) {
  return {
    id: raw?.id ?? null,
    codigoBrinco: raw?.codigoBrinco ?? '',
    nome: raw?.nome ?? '',
  }
}

function normalizeAlocacao(raw) {
  return {
    loteSectorId: raw?.loteSectorId ?? null,
    setorId: raw?.setorId ?? null,
    setorNome: raw?.setorNome ?? '',
    capacidadeMaxima: raw?.capacidadeMaxima ?? 0,
    animais: Array.isArray(raw?.animais)
      ? raw.animais.map(normalizeAnimalResumo)
      : [],
  }
}

export function normalizeLote(raw) {
  return {
    id: raw?.id ?? null,
    codigo: raw?.codigo ?? '',
    descricao: raw?.descricao ?? '',
    racaPredominante: raw?.racaPredominante ?? '',
    corBrinco: raw?.corBrinco ?? '',
    dataCriacao: raw?.dataCriacao ?? '',
    statusLote: raw?.statusLote ?? 'ATIVO',
    criadoPorNome: raw?.criadoPorNome ?? '',
    criadoPorEmail: raw?.criadoPorEmail ?? '',
    alteradoPorNome: raw?.alteradoPorNome ?? null,
    alteradoPorEmail: raw?.alteradoPorEmail ?? null,
    totalAnimais: raw?.totalAnimais ?? 0,
    alocacoes: Array.isArray(raw?.alocacoes)
      ? raw.alocacoes.map(normalizeAlocacao)
      : [],
  }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function sanitize(value) {
  return String(value ?? '').trim()
}

function toAlocacaoPayload(aloc) {
  return {
    setorId: aloc.setorId,
    animaisIds: Array.isArray(aloc.animaisIds) ? aloc.animaisIds : [],
  }
}

// ── Payload de criação ────────────────────────────────────────────────────────

function toCreatePayload(formData) {
  const corBrinco = sanitize(formData.corBrinco)
  if (!corBrinco) throw new Error('A cor do brinco é obrigatória.')

  if (!Array.isArray(formData.alocacoes) || formData.alocacoes.length === 0) {
    throw new Error('Selecione pelo menos um setor para o lote.')
  }

  const payload = {
    corBrinco,
    alocacoes: formData.alocacoes.map(toAlocacaoPayload),
  }

  const descricao = sanitize(formData.descricao)
  if (descricao) payload.descricao = descricao

  const racaPredominante = sanitize(formData.racaPredominante)
  if (racaPredominante) payload.racaPredominante = racaPredominante

  const dataCriacao = sanitize(formData.dataCriacao)
  if (dataCriacao) payload.dataCriacao = dataCriacao

  return payload
}

// ── Payload de edição ─────────────────────────────────────────────────────────

function toUpdatePayload(formData) {
  const corBrinco = sanitize(formData.corBrinco)
  if (!corBrinco) throw new Error('A cor do brinco é obrigatória.')

  const payload = { corBrinco }

  const descricao = sanitize(formData.descricao)
  payload.descricao = descricao || null

  const racaPredominante = sanitize(formData.racaPredominante)
  payload.racaPredominante = racaPredominante || null

  if (Array.isArray(formData.alocacoes) && formData.alocacoes.length > 0) {
    payload.alocacoes = formData.alocacoes.map(toAlocacaoPayload)
  }

  return payload
}

// ── API ───────────────────────────────────────────────────────────────────────

export async function listarLotesCompletos() {
  const payload = await request('/lotes')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar lotes.')
  }
  return payload.map(normalizeLote)
}

export async function listarAnimaisParaLote() {
  const payload = await request('/animais')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar animais.')
  }
  return payload.map((raw) => ({
    id: raw?.id ?? null,
    codigoBrinco: raw?.codigoBrinco ?? '',
    nome: raw?.nome ?? '',
  }))
}

export function cadastrarLote(email, formData) {
  const emailLimpo = sanitize(email)
  if (!emailLimpo) throw new Error('Usuário não identificado.')

  return request('/lotes', {
    method: 'POST',
    headers: { 'X-Usuario-Email': emailLimpo },
    body: JSON.stringify(toCreatePayload(formData)),
  })
}

export function atualizarLote(id, email, formData) {
  const emailLimpo = sanitize(email)
  if (!emailLimpo) throw new Error('Usuário não identificado.')

  return request(`/lotes/${id}`, {
    method: 'PUT',
    headers: { 'X-Usuario-Email': emailLimpo },
    body: JSON.stringify(toUpdatePayload(formData)),
  })
}

export function deletarLote(id, email) {
  const emailLimpo = sanitize(email)
  if (!emailLimpo) throw new Error('Usuário não identificado.')

  return request(`/lotes/${id}`, {
    method: 'DELETE',
    headers: { 'X-Usuario-Email': emailLimpo },
  })
}
