import { request } from '../../../integration/apiClient'

function normalizeAlerta(raw) {
  return {
    insumoId: raw?.insumoId ?? null,
    insumoNome: raw?.insumoNome ?? '',
    saldoAtual: raw?.saldoAtual ?? 0,
    estoqueMinimo: raw?.estoqueMinimo ?? 0,
    unidadeMedidaSigla: raw?.unidadeMedidaSigla ?? '',
  }
}

function normalizeTarefa(raw) {
  return {
    id: raw?.id ?? null,
    descricao: raw?.descricao ?? '',
    dataLimite: raw?.dataLimite ?? null,
    atribuidoPorNome: raw?.atribuidoPorNome ?? '',
    atribuidoPorEmail: raw?.atribuidoPorEmail ?? '',
  }
}

function normalizeResumo(raw) {
  return {
    financeiroVisivel: raw?.financeiroVisivel === true,
    totalAnimais: raw?.totalAnimais ?? null,
    totalLotes: raw?.totalLotes ?? null,
    totalSetores: raw?.totalSetores ?? null,
    vendasMensais: raw?.vendasMensais ?? null,
    gastosMensais: raw?.gastosMensais ?? null,
    lucroLiquido: raw?.lucroLiquido ?? null,
    maiorFonteReceitaLabel: raw?.maiorFonteReceitaLabel ?? null,
    maiorFonteReceitaValor: raw?.maiorFonteReceitaValor ?? null,
    menorFonteReceitaLabel: raw?.menorFonteReceitaLabel ?? null,
    menorFonteReceitaValor: raw?.menorFonteReceitaValor ?? null,
    alertasEstoque: Array.isArray(raw?.alertasEstoque) ? raw.alertasEstoque.map(normalizeAlerta) : [],
    tarefasPendentes: Array.isArray(raw?.tarefasPendentes) ? raw.tarefasPendentes.map(normalizeTarefa) : [],
  }
}

async function buscarResumo() {
  const payload = await request('/resumo')
  return normalizeResumo(payload)
}

export { buscarResumo }
