import { request } from './apiClient'

// ── Normalização ──────────────────────────────────────────────────────────

function normalizeSetor(raw) {
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
  }
}

function normalizeLote(raw) {
  return {
    id: raw?.id ?? null,
    descricao: raw?.descricao ?? '',
    // setorId é necessário para filtrar lotes por setor na MetasPage
    setorId: raw?.setor?.id ?? raw?.setorId ?? null,
  }
}

// ── Setores ────────────────────────────────────────────────────────────────

export async function listarSetores() {
  const payload = await request('/setores')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar setores.')
  }
  return payload.map(normalizeSetor)
}

// ── Lotes ──────────────────────────────────────────────────────────────────

export async function listarLotes() {
  const payload = await request('/lotes')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar lotes.')
  }
  return payload.map(normalizeLote)
}
