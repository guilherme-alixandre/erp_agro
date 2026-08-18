import { request } from '../../../integration/apiClient'

function normalizeUnidade(raw) {
  return {
    id: raw?.id ?? null,
    unidade: raw?.unidade ?? '',
  }
}

async function listarUnidadesMedida() {
  const payload = await request('/unidadeMedida')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar unidades de medida.')
  }
  return payload.map(normalizeUnidade)
}

export { listarUnidadesMedida }
