import { request } from '../../../integration/apiClient'

function normalizeOcorrencia(raw) {
  return {
    id: raw?.id ?? null,
    tipoOcorrencia: raw?.tipoOcorrencia ?? '',
    dataOcorrencia: raw?.dataOcorrencia ?? null,
    observacao: raw?.observacao ?? '',
  }
}

async function listarOcorrenciasPorAnimal(animalId) {
  const payload = await request(`/ocorrenciaAnimal?animalId=${encodeURIComponent(animalId)}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar as ocorrências.')
  }
  return payload.map(normalizeOcorrencia)
}

async function cadastrarOcorrencia(animalId, formData) {
  const body = {
    animalId: Number(animalId),
    tipoOcorrencia: formData.tipoOcorrencia,
    dataOcorrencia: formData.dataOcorrencia || null,
    observacao: formData.observacao ? formData.observacao.trim() : null,
  }
  const payload = await request('/ocorrenciaAnimal', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return normalizeOcorrencia(payload)
}

const TIPOS_OCORRENCIA = [
  { value: 'NASCIMENTO', label: 'Nascimento' },
  { value: 'OBITO', label: 'Óbito' },
  { value: 'DOENCA', label: 'Doença' },
  { value: 'VACINACAO', label: 'Vacinação' },
  { value: 'PESAGEM', label: 'Pesagem' },
]

export { listarOcorrenciasPorAnimal, cadastrarOcorrencia, TIPOS_OCORRENCIA }
