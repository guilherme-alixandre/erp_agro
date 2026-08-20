import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitizeText(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeItem(raw) {
  return {
    id: raw?.id ?? null,
    insumoId: raw?.insumoId ?? null,
    insumoNome: raw?.insumoNome ?? '',
    quantidadeRegistrada: raw?.quantidadeRegistrada ?? 0,
    unidadeRegistroSigla: raw?.unidadeRegistroSigla ?? '',
    quantidadeBaixaUnidadePrimaria: raw?.quantidadeBaixaUnidadePrimaria ?? 0,
    unidadeMedidaPrimariaSigla: raw?.unidadeMedidaPrimariaSigla ?? '',
    saldoAtualAposConsumo: raw?.saldoAtualAposConsumo ?? null,
  }
}

function normalizeConsumo(raw) {
  return {
    id: raw?.id ?? null,
    motivo: raw?.motivo ?? '',
    dataConsumo: raw?.dataConsumo ?? null,
    criadoPorEmail: raw?.criadoPorEmail ?? '',
    criadoPorNome: raw?.criadoPorNome ?? '',
    cancelado: raw?.cancelado ?? false,
    motivoCancelamento: raw?.motivoCancelamento ?? '',
    canceladoPorEmail: raw?.canceladoPorEmail ?? '',
    canceladoPorNome: raw?.canceladoPorNome ?? '',
    canceladoEm: raw?.canceladoEm ?? null,
    itens: Array.isArray(raw?.itens) ? raw.itens.map(normalizeItem) : [],
  }
}

async function listarConsumoEstoque() {
  const payload = await request('/consumo-estoque')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar o histórico de consumo de estoque.')
  }
  return payload.map(normalizeConsumo)
}

async function registrarConsumoEstoque(email, formData) {
  const body = {
    motivo: formData.motivo,
    dataConsumo: formData.dataConsumo || null,
    itens: (formData.itens ?? []).map((item) => ({
      insumoId: Number(item.insumoId),
      quantidade: Number(item.quantidade),
      unidadeMedidaId: item.unidadeMedidaId ? Number(item.unidadeMedidaId) : null,
    })),
  }
  const payload = await request('/consumo-estoque', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeConsumo(payload)
}

async function cancelarConsumoEstoque(id, email, motivoCancelamento) {
  const payload = await request(`/consumo-estoque/${id}/cancelar`, {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify({ motivoCancelamento }),
  })
  return normalizeConsumo(payload)
}

export { listarConsumoEstoque, registrarConsumoEstoque, cancelarConsumoEstoque }
