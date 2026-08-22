import { API_BASE_URL, request } from '../../../integration/apiClient'

function usuarioHeaders(email) {
  const emailLimpo = String(email ?? '').trim()
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeMedicao(raw) {
  return {
    id: raw?.id ?? null,
    loteId: raw?.loteId ?? null,
    loteCodigo: raw?.loteCodigo ?? '',
    loteDescricao: raw?.loteDescricao ?? '',
    dataMedicao: raw?.dataMedicao ?? '',
    quantidadeLancada: raw?.quantidadeLancada ?? 0,
    quantidadeConvertida: raw?.quantidadeConvertida ?? 0,
    criadoPorEmail: raw?.criadoPorEmail ?? null,
    criadoPorNome: raw?.criadoPorNome ?? null,
    criadoPorPerfil: raw?.criadoPorPerfil ?? null,
  }
}

function normalizeMeta(raw) {
  const statusRaw = raw?.status
  const status = statusRaw === 'A' ? 'ATIVA' : statusRaw === 'I' ? 'INATIVA' : (statusRaw ?? 'ATIVA')

  return {
    id: raw?.id ?? null,
    setorId: raw?.setorId ?? null,
    setorNome: raw?.setorNome ?? '',
    dataInicial: raw?.dataInicial ?? '',
    dataFinal: raw?.dataFinal ?? '',
    tipoMeta: raw?.tipoMeta ?? 'LEITE',
    quantidadeEsperada: raw?.quantidadeEsperada ?? 0,
    precoMedio: raw?.precoMedio ?? 0,
    tipoGado: raw?.tipoGado ?? null,
    status,
    quantidadeRealizada: raw?.quantidadeRealizada ?? 0,
    percentualProgresso: raw?.percentualProgresso ?? 0,
    quantidadeVendida: raw?.quantidadeVendida ?? null,
    percentualVendido: raw?.percentualVendido ?? null,
    valorRealizado: raw?.valorRealizado ?? 0,
    valorEsperado: raw?.valorEsperado ?? 0,
    medicoes: Array.isArray(raw?.medicoes) ? raw.medicoes.map(normalizeMedicao) : [],
  }
}

// ── MetaSetor ───────────────────────────────────────────────────────────

async function listarMetasPorSetor(setorId) {
  const payload = await request(`/metas-setor?setorId=${setorId}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao buscar metas.')
  }
  return payload.map(normalizeMeta)
}

function cadastrarMeta(emailUsuario, dto) {
  return request('/metas-setor', {
    method: 'POST',
    headers: usuarioHeaders(emailUsuario),
    body: JSON.stringify(dto),
  })
}

function atualizarMeta(id, emailUsuario, dto) {
  return request(`/metas-setor/${id}`, {
    method: 'PUT',
    headers: usuarioHeaders(emailUsuario),
    body: JSON.stringify(dto),
  })
}

function deletarMeta(id, emailUsuario) {
  return request(`/metas-setor/${id}`, {
    method: 'DELETE',
    headers: usuarioHeaders(emailUsuario),
  })
}

// ── MedicaoMeta ─────────────────────────────────────────────────────────

function cadastrarMedicao(emailUsuario, dto) {
  return request('/metas-setor/medicoes', {
    method: 'POST',
    headers: usuarioHeaders(emailUsuario),
    body: JSON.stringify(dto),
  })
}

function atualizarMedicao(emailUsuario, medicaoId, dto) {
  return request(`/metas-setor/medicoes/${medicaoId}`, {
    method: 'PUT',
    headers: usuarioHeaders(emailUsuario),
    body: JSON.stringify(dto),
  })
}

function deletarMedicao(medicaoId, emailUsuario) {
  return request(`/metas-setor/medicoes/${medicaoId}`, {
    method: 'DELETE',
    headers: usuarioHeaders(emailUsuario),
  })
}

// ── Validação de formulário ─────────────────────────────────────────────

function validarFormMeta(form) {
  const erros = {}

  if (!form.setorId) erros.setorId = 'Selecione um setor.'
  if (!form.dataInicial) erros.dataInicial = 'Informe a data inicial.'
  if (!form.dataFinal) erros.dataFinal = 'Informe a data final.'

  if (form.dataInicial && form.dataFinal && form.dataFinal < form.dataInicial) {
    erros.dataFinal = 'A data final não pode ser anterior à data inicial.'
  }

  if (!form.tipoMeta) erros.tipoMeta = 'Selecione o tipo de meta.'

  const qtd = Number(form.quantidadeEsperada)
  if (!form.quantidadeEsperada || isNaN(qtd) || qtd <= 0) {
    erros.quantidadeEsperada = 'Informe uma quantidade maior que zero.'
  }

  const preco = Number(form.precoMedio)
  if (!form.precoMedio || isNaN(preco) || preco <= 0) {
    erros.precoMedio = 'Informe um preço médio maior que zero.'
  }

  if (form.tipoMeta === 'ARROBA' && !form.tipoGado) {
    erros.tipoGado = 'Selecione o tipo de gado para metas de Arroba.'
  }

  return erros
}

function validarFormMedicao(form) {
  const erros = {}

  if (!form.loteId) erros.loteId = 'Selecione um lote.'
  if (!form.dataMedicao) erros.dataMedicao = 'Informe a data da medição.'

  const qtd = Number(form.quantidadeLancada)
  if (!form.quantidadeLancada || isNaN(qtd) || qtd <= 0) {
    erros.quantidadeLancada = 'Informe uma quantidade maior que zero.'
  }

  return erros
}

// ── Labels e helpers ────────────────────────────────────────────────────

const TIPOS_GADO = [
  { value: 'BOVINO_JOVEM_50', label: 'Bovino jovem — macho (50%)' },
  { value: 'NOVILHA_DESCARTE_47_5', label: 'Novilha / vaca de descarte (47,5%)' },
  { value: 'CONFINAMENTO_56', label: 'Animal em confinamento (56%)' },
]

function unidadeMeta(tipoMeta) {
  return tipoMeta === 'LEITE' ? 'L' : '@'
}

function labelQuantidade(tipoMeta) {
  if (tipoMeta === 'LEITE') return 'Quantidade (Litros)'
  return 'Peso Vivo (Kg) — convertido em arrobas'
}

function formatarMoeda(valor) {
  return Number(valor ?? 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

function formatarNumero(valor, casas = 2) {
  return Number(valor ?? 0).toLocaleString('pt-BR', {
    minimumFractionDigits: casas,
    maximumFractionDigits: casas,
  })
}

// ── Exportação ──────────────────────────────────────────────────────────

function exportarMetasCSV(metas) {
  const cabecalho = [
    'Setor', 'Tipo Meta', 'Data Inicial', 'Data Final',
    'Qtd. Esperada', 'Qtd. Realizada', 'Progresso (%)',
    'Valor Esperado (R$)', 'Valor Realizado (R$)',
  ]
  const linhas = metas.map((m) =>
    [
      m.setorNome, m.tipoMeta, m.dataInicial, m.dataFinal,
      m.quantidadeEsperada, m.quantidadeRealizada, m.percentualProgresso,
      m.valorEsperado, m.valorRealizado,
    ]
      .map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`)
      .join(';'),
  )
  const csv = '﻿' + [cabecalho.join(';'), ...linhas].join('\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'metas.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

function exportarMetasPDF(setorId) {
  window.open(`${API_BASE_URL}/metas-setor/pdf?setorId=${setorId}`, '_blank')
}

export {
  atualizarMedicao,
  atualizarMeta,
  cadastrarMedicao,
  cadastrarMeta,
  deletarMedicao,
  deletarMeta,
  exportarMetasCSV,
  exportarMetasPDF,
  formatarMoeda,
  formatarNumero,
  labelQuantidade,
  listarMetasPorSetor,
  TIPOS_GADO,
  unidadeMeta,
  validarFormMedicao,
  validarFormMeta,
}
