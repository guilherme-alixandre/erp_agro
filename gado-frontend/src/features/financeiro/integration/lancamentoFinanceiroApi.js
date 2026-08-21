import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitizeText(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeResumo(raw, ano, mes) {
  return {
    ano: raw?.ano ?? ano,
    mes: raw?.mes ?? mes,
    totalEntradas: raw?.totalEntradas ?? 0,
    totalSaidasCusto: raw?.totalSaidasCusto ?? 0,
    totalSaidasDespesa: raw?.totalSaidasDespesa ?? 0,
    totalSaidas: raw?.totalSaidas ?? 0,
    lucroLiquido: raw?.lucroLiquido ?? 0,
  }
}

async function gerarResumoMensal(email, ano, mes) {
  const payload = await request(`/financeiro/lancamentos/resumo-mensal?ano=${ano}&mes=${mes}`, {
    headers: usuarioHeaders(email),
  })
  return normalizeResumo(payload, ano, mes)
}

/** Busca o resumo dos últimos `quantidadeMeses` blocos Ano/Mês (mais antigo primeiro), para o gráfico de tendência. */
async function gerarResumoUltimosMeses(email, ano, mes, quantidadeMeses) {
  const blocos = []
  let anoAtual = ano
  let mesAtual = mes
  for (let i = 0; i < quantidadeMeses; i += 1) {
    blocos.unshift({ ano: anoAtual, mes: mesAtual })
    mesAtual -= 1
    if (mesAtual < 1) {
      mesAtual = 12
      anoAtual -= 1
    }
  }
  return Promise.all(blocos.map((bloco) => gerarResumoMensal(email, bloco.ano, bloco.mes)))
}

async function listarPorBloco(email, ano, mes) {
  const payload = await request(`/financeiro/lancamentos?ano=${ano}&mes=${mes}`, {
    headers: usuarioHeaders(email),
  })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar os lançamentos.')
  }
  return payload
}

export { gerarResumoMensal, gerarResumoUltimosMeses, listarPorBloco }
