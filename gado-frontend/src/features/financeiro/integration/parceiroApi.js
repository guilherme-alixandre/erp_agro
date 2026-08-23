import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function normalizeParceiro(raw) {
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    cpfCnpj: raw?.cpfCnpj ?? '',
    email: raw?.email ?? '',
    endereco: raw?.endereco ?? '',
    telefone: raw?.telefone ?? '',
    dataCadastro: raw?.dataCadastro ?? null,
    tipo: raw?.tipo ?? '',
  }
}

async function listarParceiros(tipo) {
  const query = tipo ? `?tipo=${encodeURIComponent(tipo)}` : ''
  const payload = await request(`/parceiros${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar parceiros.')
  }
  return payload.map(normalizeParceiro)
}

async function buscarParceiro(cpfCnpj) {
  const payload = await request(`/parceiros/${encodeURIComponent(cpfCnpj)}`)
  return normalizeParceiro(payload)
}

function toCadastroPayload(formData) {
  return {
    nome: sanitizeText(formData.nome),
    CPF_CNPJ: sanitizeText(formData.cpfCnpj),
    email: sanitizeText(formData.email) || null,
    endereco: sanitizeText(formData.endereco) || null,
    telefone: sanitizeText(formData.telefone) || null,
    tipo: formData.tipo,
  }
}

async function cadastrarParceiro(formData) {
  const payload = await request('/parceiros', {
    method: 'POST',
    body: JSON.stringify(toCadastroPayload(formData)),
  })
  return normalizeParceiro(payload)
}

async function atualizarParceiro(cpfCnpj, formData) {
  const body = {
    nome: sanitizeText(formData.nome) || undefined,
    email: sanitizeText(formData.email) || undefined,
    endereco: sanitizeText(formData.endereco) || undefined,
    telefone: sanitizeText(formData.telefone) || undefined,
    tipo: formData.tipo,
  }
  const payload = await request(`/parceiros/${encodeURIComponent(cpfCnpj)}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  return normalizeParceiro(payload)
}

function excluirParceiro(cpfCnpj) {
  return request(`/parceiros/${encodeURIComponent(cpfCnpj)}`, { method: 'DELETE' })
}

export { listarParceiros, buscarParceiro, cadastrarParceiro, atualizarParceiro, excluirParceiro }
