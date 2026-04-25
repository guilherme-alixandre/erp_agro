// URL base do backend Spring Boot.
// Se existir VITE_API_BASE_URL no ambiente, ela tem prioridade.
// Caso contrario, usamos /api para aproveitar o proxy do Vite em desenvolvimento.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'

// Função generica para chamadas HTTP ao backend.
// Ela padroniza headers JSON, trata respostas de sucesso/erro
// e devolve o corpo ja convertido para texto ou JSON.
async function request(path, options = {}) {
  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      },
      ...options,
    })
  } catch {
    throw new Error(
      `Falha ao conectar com o backend em ${API_BASE_URL}. Verifique se a API está rodando, se a URL está correta e se o CORS permite o front.`,
    )
  }

  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

  // caso dê erro para comunicar tá pelo menos inicialmente tratado isso aqui
  if (!response.ok) {
    const message =
      typeof payload === 'string'
        ? payload
        : payload?.message ?? payload?.mensagem ?? 'Erro ao comunicar com o backend.'

    throw new Error(message)
  }

  return payload
}

export { API_BASE_URL, request }
