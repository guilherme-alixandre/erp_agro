import { request } from '../../../integration/apiClient'

function normalizeEvento(raw) {
  return {
    tipoCusto: raw?.tipoCusto ?? '',
    dataEvento: raw?.dataEvento ?? null,
    descricao: raw?.descricao ?? '',
    valor: Number(raw?.valor ?? 0),
  }
}

function normalizeCustoVida(raw) {
  return {
    animalId: raw?.animalId ?? null,
    totalAquisicao: Number(raw?.totalAquisicao ?? 0),
    totalAlimentacao: Number(raw?.totalAlimentacao ?? 0),
    totalVacinacao: Number(raw?.totalVacinacao ?? 0),
    totalGeral: Number(raw?.totalGeral ?? 0),
    eventos: Array.isArray(raw?.eventos) ? raw.eventos.map(normalizeEvento) : [],
  }
}

async function buscarCustoVidaAnimal(animalId) {
  const payload = await request(`/animais/${encodeURIComponent(animalId)}/custo-vida`)
  return normalizeCustoVida(payload)
}

export { buscarCustoVidaAnimal }
