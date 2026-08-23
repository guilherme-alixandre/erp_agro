import { request } from '../../../integration/apiClient'

// Autenticação agora é feita via o token JWT anexado automaticamente pelo apiClient.
function usuarioHeaders() {
  return {}
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

async function listarConsumoPorSetor(setorId, dataInicio, dataFim) {
  if (!setorId) return []
  const params = new URLSearchParams({ setorId: String(setorId) })
  if (dataInicio) params.set('dataInicio', dataInicio)
  if (dataFim) params.set('dataFim', dataFim)
  const payload = await request(`/consumos-insumo?${params.toString()}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar o histórico de consumo.')
  }
  return payload.map(normalizeConsumo)
}

async function editarConsumo(id, email, formData) {
  const body = {
    quantidade: Number(formData.quantidade),
    unidadeMedidaId: formData.unidadeMedidaId ? Number(formData.unidadeMedidaId) : null,
    dataConsumo: formData.dataConsumo || null,
  }
  const payload = await request(`/consumos-insumo/${id}`, {
    method: 'PUT',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeConsumo(payload)
}

async function resumoPorSetorEPeriodo(setorId, dataInicio, dataFim) {
  const params = new URLSearchParams({ setorId: String(setorId), dataInicio, dataFim })
  const payload = await request(`/consumos-insumo/resumo?${params.toString()}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao gerar o resumo.')
  }
  return payload.map((item) => ({
    insumoId: item?.insumoId ?? null,
    insumoNome: item?.insumoNome ?? '',
    quantidadeTotal: item?.quantidadeTotal ?? 0,
    unidadeSigla: item?.unidadeSigla ?? '',
  }))
}

export { registrarConsumo, listarConsumoPorSetor, editarConsumo, resumoPorSetorEPeriodo }
