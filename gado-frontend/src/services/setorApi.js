import { request } from './apiClient'

// ── Normalização resumida (App.jsx — estado compartilhado) ────────────────

function normalizeSetorResumo(raw) {
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    capacidadeMaxima: raw?.capacidadeMaxima ?? 0,
  }
}

function normalizeLoteResumo(raw) {
  return {
    id: raw?.id ?? null,
    descricao: raw?.descricao ?? '',
    setorId: raw?.setor?.id ?? raw?.setorId ?? null,
  }
}

// ── Normalização completa (SetoresPage) ───────────────────────────────────

function normalizeLoteSetor(raw) {
  return {
    loteSectorId: raw?.loteSectorId ?? raw?.id ?? null,
    loteId: raw?.loteId ?? raw?.lote?.id ?? null,
    loteCodigo: raw?.loteCodigo ?? raw?.lote?.codigo ?? '',
    loteCorBrinco: raw?.loteCorBrinco ?? raw?.lote?.corBrinco ?? '',
    quantidadeAnimais: raw?.quantidadeAnimais ?? raw?.totalAnimais ?? 0,
  }
}

export function normalizeSetorCompleto(raw) {
  const statusRaw = raw?.status
  const status = statusRaw === 'A' ? 'ATIVO' : statusRaw === 'I' ? 'INATIVO' : (statusRaw ?? 'ATIVO')

  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    capacidadeMaxima: raw?.capacidadeMaxima ?? 0,
    tipo: raw?.tipo ?? '',
    metaTexto: raw?.metaTexto ?? '',
    status,
    criadoPorNome: raw?.criadoPorNome ?? '',
    criadoPorEmail: raw?.criadoPorEmail ?? '',
    alteradoPorNome: raw?.alteradoPorNome ?? null,
    alteradoPorEmail: raw?.alteradoPorEmail ?? null,
    lotes: Array.isArray(raw?.lotes) ? raw.lotes.map(normalizeLoteSetor) : [],
  }
}

// ── Helpers ────────────────────────────────────────────────────────────────

function sanitize(value) {
  return String(value ?? '').trim()
}

function toCreatePayload(formData) {
  const nome = sanitize(formData.nome)
  if (!nome) throw new Error('O nome é obrigatório.')

  const capacidadeMaxima = Number(formData.capacidadeMaxima)
  if (!formData.capacidadeMaxima || isNaN(capacidadeMaxima) || capacidadeMaxima <= 0) {
    throw new Error('Informe uma capacidade máxima válida.')
  }

  const tipo = sanitize(formData.tipo)
  if (!tipo) throw new Error('Selecione o tipo do setor.')

  const payload = { nome, capacidadeMaxima, tipo }

  const metaTexto = sanitize(formData.metaTexto)
  if (metaTexto) payload.metaTexto = metaTexto

  return payload
}

function toUpdatePayload(formData) {
  const nome = sanitize(formData.nome)
  if (!nome) throw new Error('O nome é obrigatório.')

  const capacidadeMaxima = Number(formData.capacidadeMaxima)
  if (!formData.capacidadeMaxima || isNaN(capacidadeMaxima) || capacidadeMaxima <= 0) {
    throw new Error('Informe uma capacidade máxima válida.')
  }

  const tipo = sanitize(formData.tipo)
  if (!tipo) throw new Error('Selecione o tipo do setor.')

  const metaTexto = sanitize(formData.metaTexto)

  return {
    nome,
    capacidadeMaxima,
    tipo,
    metaTexto: metaTexto || null,
  }
}

// ── API — compartilhada (App.jsx) ──────────────────────────────────────────

export async function listarSetores() {
  const payload = await request('/setores')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar setores.')
  }
  return payload.map(normalizeSetorResumo)
}

export async function listarLotes() {
  const payload = await request('/lotes')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar lotes.')
  }
  return payload.map(normalizeLoteResumo)
}

// ── API — SetoresPage ──────────────────────────────────────────────────────

export async function listarSetoresCompletos() {
  const payload = await request('/setores')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar setores.')
  }
  return payload.map(normalizeSetorCompleto)
}

export function cadastrarSetor(email, formData) {
  const emailLimpo = sanitize(email)
  if (!emailLimpo) throw new Error('Usuário não identificado.')

  return request('/setores', {
    method: 'POST',
    headers: {
      'X-Usuario-Email': emailLimpo,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(toCreatePayload(formData)),
  })
}

export function atualizarSetor(id, email, formData) {
  const emailLimpo = sanitize(email)
  if (!emailLimpo) throw new Error('Usuário não identificado.')

  return request(`/setores/${id}`, {
    method: 'PUT',
    headers: {
      'X-Usuario-Email': emailLimpo,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(toUpdatePayload(formData)),
  })
}

export function deletarSetor(id, email) {
  const emailLimpo = sanitize(email)
  if (!emailLimpo) throw new Error('Usuário não identificado.')

  return request(`/setores/${id}`, {
    method: 'DELETE',
    headers: { 'X-Usuario-Email': emailLimpo },
  })
}
