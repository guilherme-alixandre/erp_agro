import { API_BASE_URL, request } from '../../../integration/apiClient'

function sanitize(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitize(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

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
    animais: Array.isArray(raw?.animais) ? raw.animais.map(normalizeAnimalResumo) : [],
  }
}

function normalizeLote(raw) {
  const statusRaw = raw?.status
  const statusLote = statusRaw === 'A' ? 'ATIVO' : statusRaw === 'I' ? 'INATIVO' : (statusRaw ?? 'ATIVO')

  return {
    id: raw?.id ?? null,
    codigo: raw?.codigo ?? '',
    descricao: raw?.descricao ?? '',
    racaPredominante: raw?.racaPredominante ?? '',
    corBrinco: raw?.corBrinco ?? '',
    dataCriacao: raw?.dataCriacao ?? '',
    statusLote,
    criadoPorNome: raw?.criadoPorNome ?? '',
    criadoPorEmail: raw?.criadoPorEmail ?? '',
    alteradoPorNome: raw?.alteradoPorNome ?? null,
    alteradoPorEmail: raw?.alteradoPorEmail ?? null,
    totalAnimais: raw?.totalAnimais ?? 0,
    alocacoes: Array.isArray(raw?.alocacoes) ? raw.alocacoes.map(normalizeAlocacao) : [],
  }
}

function toAlocacaoPayload(aloc) {
  return {
    setorId: aloc.setorId,
    animaisIds: Array.isArray(aloc.animaisIds) ? aloc.animaisIds : [],
  }
}

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

async function listarLotes() {
  const payload = await request('/lotes')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar lotes.')
  }
  return payload.map(normalizeLote)
}

async function listarAnimaisParaLote() {
  const payload = await request('/animais')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar animais.')
  }
  return payload
    .filter((raw) => raw?.id != null)
    .map((raw) => ({
      id: raw.id,
      codigoBrinco: raw?.codigoBrinco ?? '',
      nome: raw?.nome ?? '',
      statusAnimal: raw?.statusAnimal ?? 'ATIVO',
    }))
}

function cadastrarLote(email, formData) {
  return request('/lotes', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(toCreatePayload(formData)),
  })
}

function atualizarLote(id, email, formData) {
  return request(`/lotes/${id}`, {
    method: 'PUT',
    headers: usuarioHeaders(email),
    body: JSON.stringify(toUpdatePayload(formData)),
  })
}

function deletarLote(id, email) {
  return request(`/lotes/${id}`, {
    method: 'DELETE',
    headers: usuarioHeaders(email),
  })
}

function transferirAnimal(email, animalId, loteDestinoId, setorDestinoId) {
  return request('/lotes/transferir-animal', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify({ animalId, loteDestinoId, setorDestinoId }),
  })
}

function exportarLotesCSV(lotes) {
  const cabecalho = ['Código', 'Descrição', 'Raça Predominante', 'Cor Brinco', 'Total Animais', 'Data Criação', 'Status', 'Criado Por']
  const linhas = lotes.map((l) =>
    [l.codigo, l.descricao ?? '', l.racaPredominante ?? '', l.corBrinco, l.totalAnimais, l.dataCriacao ?? '', l.statusLote, l.criadoPorNome]
      .map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`)
      .join(';'),
  )
  const csv = '﻿' + [cabecalho.join(';'), ...linhas].join('\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'lotes.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function exportarLotesPDF() {
  window.open(`${API_BASE_URL}/lotes/pdf`, '_blank')
}

async function buscarCustoRacaoLote(loteId) {
  const payload = await request(`/lotes/${loteId}/custo-racao`)
  return {
    loteId: payload?.loteId ?? loteId,
    custoTotalAcumulado: payload?.custoTotalAcumulado ?? 0,
    custoPorAnimal: payload?.custoPorAnimal ?? 0,
  }
}

export {
  atualizarLote,
  buscarCustoRacaoLote,
  cadastrarLote,
  deletarLote,
  exportarLotesCSV,
  exportarLotesPDF,
  listarAnimaisParaLote,
  listarLotes,
  transferirAnimal,
}
