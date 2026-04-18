// URL base do backend Spring Boot.
// Se existir VITE_API_BASE_URL no ambiente, ela tem prioridade.
// Caso contrario, usamos o backend local em localhost:8080/api.
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

// Função generica para chamadas HTTP ao backend.
// Ela padroniza headers JSON, trata respostas de sucesso/erro
// e devolve o corpo ja convertido para texto ou JSON.
async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    ...options,
  })

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
