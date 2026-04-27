// URL base do backend Spring Boot.
// Se existir VITE_API_BASE_URL no ambiente, ela tem prioridade.
// Caso contrario, usamos '/api' em dev (proxy do Vite) e localhost:8080/api em build.
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  (import.meta.env.DEV ? '/api' : 'http://localhost:8080/api')

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
    let message = ''

    if (typeof payload === 'string') {
      message = payload
    } else if (payload && typeof payload === 'object') {
      const mainMessage =
        payload.message ?? payload.mensagem ?? payload.error ?? payload.detail
      if (typeof mainMessage === 'string' && mainMessage.trim()) {
        message = mainMessage.trim()
      }

      const fieldErrors = payload.errors ?? payload.erros
      if (
        !message &&
        Array.isArray(fieldErrors) &&
        fieldErrors.length > 0
      ) {
        const firstFieldError = fieldErrors[0]
        if (typeof firstFieldError === 'string') {
          message = firstFieldError
        } else if (firstFieldError && typeof firstFieldError === 'object') {
          message =
            firstFieldError.message ?? firstFieldError.mensagem ?? message
        }
      }
    }

    const fallbackMessage = `Falha na requisição (${response.status} ${response.statusText}).`

    throw new Error(message || fallbackMessage)
  }

  return payload
}

export { API_BASE_URL, request }
