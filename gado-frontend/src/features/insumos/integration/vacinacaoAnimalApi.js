import { request } from '../../../integration/apiClient'

// Autenticação agora é feita via o token JWT anexado automaticamente pelo apiClient.
function usuarioHeaders() {
  return {}
}

function normalizeAnimalResumo(raw) {
  return {
    id: raw?.id ?? null,
    codigoBrinco: raw?.codigoBrinco ?? '',
    racaNome: raw?.racaNome ?? '',
  }
}

function normalizeVacinacao(raw) {
  return {
    id: raw?.id ?? null,
    insumoId: raw?.insumoId ?? null,
    insumoNome: raw?.insumoNome ?? '',
    quantidadePorAnimal: raw?.quantidadePorAnimal ?? 0,
    unidadeRegistroSigla: raw?.unidadeRegistroSigla ?? '',
    quantidadeBaixaPorAnimalUnidadePrimaria: raw?.quantidadeBaixaPorAnimalUnidadePrimaria ?? 0,
    unidadeMedidaPrimariaSigla: raw?.unidadeMedidaPrimariaSigla ?? '',
    quantidadeTotalBaixaUnidadePrimaria: raw?.quantidadeTotalBaixaUnidadePrimaria ?? 0,
    saldoAtualAposAplicacao: raw?.saldoAtualAposAplicacao ?? null,
    totalAnimais: raw?.totalAnimais ?? 0,
    animais: Array.isArray(raw?.animais) ? raw.animais.map(normalizeAnimalResumo) : [],
    loteId: raw?.loteId ?? null,
    loteCodigo: raw?.loteCodigo ?? '',
    dataAplicacao: raw?.dataAplicacao ?? null,
    criadoPorEmail: raw?.criadoPorEmail ?? '',
    criadoPorNome: raw?.criadoPorNome ?? '',
    cancelado: raw?.cancelado ?? false,
    motivoCancelamento: raw?.motivoCancelamento ?? '',
    canceladoPorEmail: raw?.canceladoPorEmail ?? '',
    canceladoPorNome: raw?.canceladoPorNome ?? '',
    canceladoEm: raw?.canceladoEm ?? null,
  }
}

async function listarVacinacoes(dataInicio, dataFim) {
  const params = new URLSearchParams()
  if (dataInicio) params.set('dataInicio', dataInicio)
  if (dataFim) params.set('dataFim', dataFim)
  const query = params.toString() ? `?${params.toString()}` : ''
  const payload = await request(`/vacinacoes-animal${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar o histórico de vacinação.')
  }
  return payload.map(normalizeVacinacao)
}

async function listarVacinacoesPorAnimal(animalId) {
  const payload = await request(`/vacinacoes-animal?animalId=${encodeURIComponent(animalId)}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar o histórico de vacinação do animal.')
  }
  return payload.map(normalizeVacinacao)
}

async function registrarVacinacao(email, formData) {
  const body = {
    insumoId: Number(formData.insumoId),
    quantidadePorAnimal: Number(formData.quantidadePorAnimal),
    unidadeMedidaId: formData.unidadeMedidaId ? Number(formData.unidadeMedidaId) : null,
    dataAplicacao: formData.dataAplicacao || null,
    loteId: formData.loteId ? Number(formData.loteId) : null,
    animalIds: (formData.animalIds ?? []).map(Number),
  }
  const payload = await request('/vacinacoes-animal', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeVacinacao(payload)
}

async function editarVacinacao(id, email, formData) {
  const body = {
    quantidadePorAnimal: Number(formData.quantidadePorAnimal),
    unidadeMedidaId: formData.unidadeMedidaId ? Number(formData.unidadeMedidaId) : null,
    dataAplicacao: formData.dataAplicacao || null,
  }
  const payload = await request(`/vacinacoes-animal/${id}`, {
    method: 'PUT',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeVacinacao(payload)
}

async function cancelarVacinacao(id, email, motivoCancelamento) {
  const payload = await request(`/vacinacoes-animal/${id}/cancelar`, {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify({ motivoCancelamento }),
  })
  return normalizeVacinacao(payload)
}

async function resumoVacinacaoPorPeriodo(dataInicio, dataFim) {
  const params = new URLSearchParams({ dataInicio, dataFim })
  const payload = await request(`/vacinacoes-animal/resumo?${params.toString()}`)
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

export {
  listarVacinacoes,
  listarVacinacoesPorAnimal,
  registrarVacinacao,
  editarVacinacao,
  cancelarVacinacao,
  resumoVacinacaoPorPeriodo,
}
