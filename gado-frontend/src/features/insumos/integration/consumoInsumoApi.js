import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitizeText(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeConsumo(raw) {
  return {
    id: raw?.id ?? null,
    insumoId: raw?.insumoId ?? null,
    insumoNome: raw?.insumoNome ?? '',
    setorId: raw?.setorId ?? null,
    setorNome: raw?.setorNome ?? '',
    quantidadeRegistrada: raw?.quantidadeRegistrada ?? 0,
    unidadeRegistroSigla: raw?.unidadeRegistroSigla ?? '',
    quantidadeBaixaUnidadePrimaria: raw?.quantidadeBaixaUnidadePrimaria ?? 0,
    unidadeMedidaPrimariaSigla: raw?.unidadeMedidaPrimariaSigla ?? '',
    totalAnimaisSetor: raw?.totalAnimaisSetor ?? 0,
    consumoPorAnimal: raw?.consumoPorAnimal ?? null,
    saldoAtualAposConsumo: raw?.saldoAtualAposConsumo ?? null,
    dataConsumo: raw?.dataConsumo ?? null,
    registradoPorEmail: raw?.registradoPorEmail ?? '',
    registradoPorNome: raw?.registradoPorNome ?? '',
  }
}

async function registrarConsumo(email, formData) {
  const body = {
    insumoId: Number(formData.insumoId),
    setorId: Number(formData.setorId),
    quantidade: Number(formData.quantidade),
    unidadeMedidaId: formData.unidadeMedidaId ? Number(formData.unidadeMedidaId) : null,
    dataConsumo: formData.dataConsumo || null,
  }
  const payload = await request('/consumos-insumo', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeConsumo(payload)
}

async function listarConsumoPorSetor(setorId) {
  if (!setorId) return []
  const payload = await request(`/consumos-insumo?setorId=${encodeURIComponent(setorId)}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar o histórico de consumo.')
  }
  return payload.map(normalizeConsumo)
}

export { registrarConsumo, listarConsumoPorSetor }
