// URL base do backend Spring Boot.
// Se existir VITE_API_BASE_URL no ambiente, ela tem prioridade.
// Caso contrário, usamos '/api' em dev (proxy do Vite) e localhost:8080/api em build.
const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  (import.meta.env.DEV ? '/api' : 'http://localhost:8080/api')

function friendlyByStatus(status) {
  if (status >= 500 && status <= 599) {
    return 'O servidor encontrou um problema ao processar sua solicitação. Tente novamente em instantes.'
  }
  if (status === 404) {
    return 'Recurso não encontrado.'
  }
  if (status === 401 || status === 403) {
    return 'Você não tem permissão para realizar esta ação.'
  }
  if (status === 408 || status === 504) {
    return 'Tempo de resposta esgotado. Tente novamente.'
  }
  if (status === 503) {
    return 'Servidor temporariamente indisponível. Tente novamente em instantes.'
  }
  if (status >= 400 && status <= 499) {
    return 'Não foi possível concluir a operação. Verifique os dados e tente novamente.'
  }
  return 'Não foi possível concluir a operação. Tente novamente.'
}

function looksLikeStacktrace(text) {
  return /\bException\b|\bat [a-z0-9_.$]+\(/i.test(text)
}

function extractBackendMessage(payload) {
  if (typeof payload === 'string') {
    const trimmed = payload.trim()
    return trimmed && !looksLikeStacktrace(trimmed) ? trimmed : ''
  }

  if (payload && typeof payload === 'object') {
    const main =
      payload.mensagem ?? payload.message ?? payload.error ?? payload.detail
    if (typeof main === 'string' && main.trim() && !looksLikeStacktrace(main)) {
      return main.trim()
    }

    const fieldErrors = payload.errors ?? payload.erros
    if (Array.isArray(fieldErrors) && fieldErrors.length > 0) {
      const first = fieldErrors[0]
      if (typeof first === 'string' && first.trim()) return first.trim()
      if (first && typeof first === 'object') {
        const m = first.message ?? first.mensagem
        if (typeof m === 'string' && m.trim()) return m.trim()
      }
    }
  }

  return ''
}

function tryParseJson(text) {
  if (typeof text !== 'string') return null
  const trimmed = text.trim()
  if (!trimmed) return null
  if (!(trimmed.startsWith('{') || trimmed.startsWith('['))) return null

  try {
    return JSON.parse(trimmed)
  } catch {
    return null
  }
}

async function request(path, options = {}) {
  let response

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      },
      ...options,
    })
  } catch {
    throw new Error(
      'Não foi possível conectar ao servidor. Verifique sua conexão e se o backend está rodando.',
    )
  }

  const contentType = response.headers.get('content-type') ?? ''

  // Alguns endpoints / proxies podem devolver JSON com Content-Type incorreto (ex.: text/plain; charset=UTF-8).
  // Por isso, sempre tentamos fazer parse de JSON a partir do texto.
  let payload = ''
  try {
    const text = await response.text()
    const parsed = tryParseJson(text)
    payload = parsed ?? text
  } catch {
    payload = ''
  }

  if (!response.ok) {
    const backendMessage = extractBackendMessage(payload)
    throw new Error(backendMessage || friendlyByStatus(response.status))
  }

  // Se o backend retornou texto mas com Content-Type json, ainda assim tentamos parsear.
  if (typeof payload === 'string' && contentType.includes('application/json')) {
    const parsed = tryParseJson(payload)
    if (parsed !== null) return parsed
  }

  return payload
}

export { API_BASE_URL, request }

