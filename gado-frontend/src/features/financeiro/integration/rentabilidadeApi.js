import { downloadFile, request } from '../../../integration/apiClient'

// Autenticação agora é feita via o token JWT anexado automaticamente pelo apiClient.
function usuarioHeaders() {
  return {}
}

function periodoQuery(dataInicio, dataFim) {
  return `dataInicio=${encodeURIComponent(dataInicio)}&dataFim=${encodeURIComponent(dataFim)}`
}

function normalizeResumoPeriodo(raw) {
  return {
    dataInicio: raw?.dataInicio ?? null,
    dataFim: raw?.dataFim ?? null,
    totalEntradas: raw?.totalEntradas ?? 0,
    totalSaidasCusto: raw?.totalSaidasCusto ?? 0,
    totalSaidasDespesa: raw?.totalSaidasDespesa ?? 0,
    totalSaidas: raw?.totalSaidas ?? 0,
    lucroLiquido: raw?.lucroLiquido ?? 0,
    porMes: Array.isArray(raw?.porMes)
      ? raw.porMes.map((m) => ({
          ano: m?.ano,
          mes: m?.mes,
          totalEntradas: m?.totalEntradas ?? 0,
          totalSaidasCusto: m?.totalSaidasCusto ?? 0,
          totalSaidasDespesa: m?.totalSaidasDespesa ?? 0,
        }))
      : [],
  }
}

function normalizeContaParceiro(raw) {
  return {
    parceiroId: raw?.parceiroId ?? null,
    parceiroNome: raw?.parceiroNome ?? '',
    totalAPagar: raw?.totalAPagar ?? 0,
    totalAReceber: raw?.totalAReceber ?? 0,
    qtdNfsPagar: raw?.qtdNfsPagar ?? 0,
    qtdNfsReceber: raw?.qtdNfsReceber ?? 0,
    proximoVencimento: raw?.proximoVencimento ?? null,
  }
}

function normalizeProdutoVendido(raw) {
  return {
    nome: raw?.nome ?? '',
    quantidade: raw?.quantidade ?? 0,
    unidade: raw?.unidade ?? '',
    valorTotal: raw?.valorTotal ?? 0,
  }
}

function normalizeGastoGrupo(raw) {
  return {
    grupoNome: raw?.grupoNome ?? '',
    valorTotal: raw?.valorTotal ?? 0,
  }
}

async function gerarResumoPeriodo(email, dataInicio, dataFim) {
  const payload = await request(`/financeiro/rentabilidade/resumo?${periodoQuery(dataInicio, dataFim)}`, {
    headers: usuarioHeaders(email),
  })
  return normalizeResumoPeriodo(payload)
}

async function listarContasPorParceiro(email, dataInicio, dataFim) {
  const payload = await request(`/financeiro/rentabilidade/contas-parceiro?${periodoQuery(dataInicio, dataFim)}`, {
    headers: usuarioHeaders(email),
  })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar contas por parceiro.')
  }
  return payload.map(normalizeContaParceiro)
}

async function listarVendasPorProduto(email, dataInicio, dataFim) {
  const payload = await request(`/financeiro/rentabilidade/vendas-produto?${periodoQuery(dataInicio, dataFim)}`, {
    headers: usuarioHeaders(email),
  })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar vendas por produto.')
  }
  return payload.map(normalizeProdutoVendido)
}

async function listarGastosPorGrupo(email, dataInicio, dataFim) {
  const payload = await request(`/financeiro/rentabilidade/gastos-grupo?${periodoQuery(dataInicio, dataFim)}`, {
    headers: usuarioHeaders(email),
  })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar gastos por grupo.')
  }
  return payload.map(normalizeGastoGrupo)
}

function exportarRentabilidadePDF(dataInicio, dataFim) {
  return downloadFile(`/financeiro/rentabilidade/pdf?${periodoQuery(dataInicio, dataFim)}`, 'relatorio-rentabilidade.pdf')
}

function exportarContasParceiroCSV(contas) {
  const cabecalho = ['Parceiro', 'Total a Pagar', 'Total a Receber', 'Qtd. NFs a Pagar', 'Qtd. NFs a Receber', 'Próximo Vencimento']
  const linhas = contas.map((c) =>
    [c.parceiroNome, c.totalAPagar, c.totalAReceber, c.qtdNfsPagar, c.qtdNfsReceber, c.proximoVencimento ?? '']
      .map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`)
      .join(';'),
  )
  const csv = '﻿' + [cabecalho.join(';'), ...linhas].join('\n')

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'contas-a-pagar-e-receber.csv'
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

export {
  gerarResumoPeriodo,
  listarContasPorParceiro,
  listarVendasPorProduto,
  listarGastosPorGrupo,
  exportarRentabilidadePDF,
  exportarContasParceiroCSV,
}
