import { API_BASE_URL, request } from '../../../integration/apiClient'

function sanitize(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitize(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeLoteResumo(raw) {
  return {
    loteSectorId: raw?.loteSectorId ?? null,
    loteId: raw?.loteId ?? null,
    loteCodigo: raw?.loteCodigo ?? '',
    loteCorBrinco: raw?.loteCorBrinco ?? '',
    quantidadeAnimais: raw?.quantidadeAnimais ?? 0,
  }
}

function normalizeSetor(raw) {
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
    lotes: Array.isArray(raw?.lotes) ? raw.lotes.map(normalizeLoteResumo) : [],
  }
}

function toPayload(formData) {
  const nome = sanitize(formData.nome)
  if (!nome) throw new Error('O nome é obrigatório.')

  const capacidadeMaxima = Number(formData.capacidadeMaxima)
  if (!formData.capacidadeMaxima || isNaN(capacidadeMaxima) || capacidadeMaxima <= 0) {
    throw new Error('Informe uma capacidade máxima válida.')
  }

  const tipo = sanitize(formData.tipo)
  if (!tipo) throw new Error('Selecione o tipo do setor.')

  return {
    nome,
    capacidadeMaxima,
    tipo,
    metaTexto: sanitize(formData.metaTexto) || null,
  }
}

async function listarSetores() {
  const payload = await request('/setores')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar setores.')
  }
  return payload.map(normalizeSetor)
}

function cadastrarSetor(email, formData) {
  return request('/setores', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(toPayload(formData)),
  })
}

function atualizarSetor(id, email, formData) {
  return request(`/setores/${id}`, {
    method: 'PUT',
    headers: usuarioHeaders(email),
    body: JSON.stringify(toPayload(formData)),
  })
}

function deletarSetor(id) {
  return request(`/setores/${id}`, { method: 'DELETE' })
}

function exportarSetoresCSV(setores) {
  const cabecalho = ['Nome', 'Tipo', 'Capacidade Máxima', 'Status', 'Meta Texto', 'Criado Por']
  const linhas = setores.map((s) =>
    [s.nome, s.tipo, s.capacidadeMaxima, s.status, s.metaTexto ?? '', s.criadoPorNome]
      .map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`)
      .join(';'),
  )
  const csv = '﻿' + [cabecalho.join(';'), ...linhas].join('\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'setores.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function exportarSetoresPDF() {
  window.open(`${API_BASE_URL}/setores/pdf`, '_blank')
}

export {
  atualizarSetor,
  cadastrarSetor,
  deletarSetor,
  exportarSetoresCSV,
  exportarSetoresPDF,
  listarSetores,
  normalizeSetor,
}
