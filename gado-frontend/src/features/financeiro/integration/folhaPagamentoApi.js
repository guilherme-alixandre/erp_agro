import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitizeText(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeFuncionario(raw) {
  return {
    id: raw?.id ?? null,
    nomeCompleto: raw?.nomeCompleto ?? '',
    cpf: raw?.cpf ?? '',
    cargo: raw?.cargo ?? '',
    dataAdmissao: raw?.dataAdmissao ?? null,
    dataDemissao: raw?.dataDemissao ?? null,
    salarioBase: raw?.salarioBase ?? 0,
    percentualInss: raw?.percentualInss ?? 0,
    percentualFgts: raw?.percentualFgts ?? 0,
    valorValeTransporte: raw?.valorValeTransporte ?? null,
    valorValeAlimentacao: raw?.valorValeAlimentacao ?? null,
    valorPlanoSaude: raw?.valorPlanoSaude ?? null,
    naturezaFinanceira: raw?.naturezaFinanceira ?? '',
  }
}

function normalizePagamento(raw) {
  return {
    id: raw?.id ?? null,
    funcionarioId: raw?.funcionarioId ?? null,
    funcionarioNome: raw?.funcionarioNome ?? '',
    anoReferencia: raw?.anoReferencia ?? null,
    mesReferencia: raw?.mesReferencia ?? null,
    dataPagamento: raw?.dataPagamento ?? null,
    statusPagamento: raw?.statusPagamento ?? '',
    valorBruto: raw?.valorBruto ?? 0,
    descontoInss: raw?.descontoInss ?? 0,
    descontoOutros: raw?.descontoOutros ?? 0,
    encargoFgts: raw?.encargoFgts ?? 0,
    valorBeneficios: raw?.valorBeneficios ?? 0,
    valorLiquido: raw?.valorLiquido ?? 0,
    naturezaFinanceiraSnapshot: raw?.naturezaFinanceiraSnapshot ?? '',
  }
}

async function listarFuncionarios(email) {
  const payload = await request('/folha-pagamento/funcionarios', { headers: usuarioHeaders(email) })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar os funcionários.')
  }
  return payload.map(normalizeFuncionario)
}

async function cadastrarFuncionario(email, formData) {
  const body = {
    nomeCompleto: sanitizeText(formData.nomeCompleto),
    cpf: sanitizeText(formData.cpf),
    cargo: sanitizeText(formData.cargo),
    dataAdmissao: formData.dataAdmissao || null,
    salarioBase: Number(formData.salarioBase),
    percentualInss: Number(formData.percentualInss),
    percentualFgts: Number(formData.percentualFgts),
    valorValeTransporte: formData.valorValeTransporte !== '' ? Number(formData.valorValeTransporte) : null,
    valorValeAlimentacao: formData.valorValeAlimentacao !== '' ? Number(formData.valorValeAlimentacao) : null,
    valorPlanoSaude: formData.valorPlanoSaude !== '' ? Number(formData.valorPlanoSaude) : null,
    naturezaFinanceira: formData.naturezaFinanceira,
  }
  const payload = await request('/folha-pagamento/funcionarios', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeFuncionario(payload)
}

async function listarPagamentosPorBloco(email, ano, mes) {
  const payload = await request(`/folha-pagamento/pagamentos?ano=${ano}&mes=${mes}`, {
    headers: usuarioHeaders(email),
  })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar os pagamentos do mês.')
  }
  return payload.map(normalizePagamento)
}

async function lancarPagamento(email, formData) {
  const body = {
    funcionarioId: Number(formData.funcionarioId),
    anoReferencia: Number(formData.anoReferencia),
    mesReferencia: Number(formData.mesReferencia),
    dataPagamento: formData.dataPagamento || null,
    descontoOutros: formData.descontoOutros !== '' ? Number(formData.descontoOutros) : null,
  }
  const payload = await request('/folha-pagamento/pagamentos', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizePagamento(payload)
}

export {
  listarFuncionarios,
  cadastrarFuncionario,
  listarPagamentosPorBloco,
  lancarPagamento,
}
