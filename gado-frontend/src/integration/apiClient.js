// Configuração baseada no ambiente
const API_BASE_URL = 'http://localhost:8080/api'


const ERROR_MESSAGES = {
    400: 'Não foi possível concluir a operação. Verifique os dados e tente novamente.',
    401: 'Você não tem permissão para realizar esta ação.',
    403: 'Você não tem permissão para realizar esta ação.',
    404: 'Recurso não encontrado.',
    408: 'Tempo de resposta esgotado. Tente novamente.',
    503: 'Servidor temporariamente indisponível. Tente novamente em instantes.',
    504: 'Tempo de resposta esgotado. Tente novamente.',
    500: 'O servidor encontrou um problema. Tente novamente em instantes.',
    default: 'Não foi possível concluir a operação. Tente novamente.',
}

// detecta se o texto parece um erro Java (stacktrace)
function looksLikeStacktrace(text) {
    return /\bException\b|\bat [a-z0-9_.$]+\(/i.test(text)
}

// Tenta fazer parse JSON seguro
function tryParseJson(text) {
    if (typeof text !== 'string') return null
    const trimmed = text.trim()
    if (!trimmed || !(trimmed.startsWith('{') || trimmed.startsWith('['))) return null
    try {
        return JSON.parse(trimmed)
    } catch {
        return null
    }
}

// Extrai mensagem de erro do backend (ignora stacktraces)
function extractBackendMessage(payload) {
    if (typeof payload === 'string') {
        const trimmed = payload.trim()
        return trimmed && !looksLikeStacktrace(trimmed) ? trimmed : ''
    }

    if (payload && typeof payload === 'object') {
        // Tenta extrair mensagem principal (suporta várias convenções)
        const message = payload.mensagem ?? payload.message ?? payload.error ?? payload.detail
        if (typeof message === 'string' && message.trim() && !looksLikeStacktrace(message)) {
            return message.trim()
        }

        // Tenta extrair de array de erros de validação
        const errors = payload.errors ?? payload.erros
        if (Array.isArray(errors) && errors.length > 0) {
            const first = errors[0]
            if (typeof first === 'string' && first.trim()) return first.trim()
            if (first?.message || first?.mensagem) {
                const msg = first.message ?? first.mensagem
                if (typeof msg === 'string' && msg.trim()) return msg.trim()
            }
        }
    }

    return ''
}

// Faz requisição HTTP com tratamento robusto
async function request(path, options = {}) {
    let response

    try {
        response = await fetch(`${API_BASE_URL}${path}`, {
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json',
                ...(options.headers ?? {}),
            },
            ...options,
        })
    } catch {
        throw new Error(
            'Não foi possível conectar ao servidor. Verifique sua conexão e se o backend está rodando.'
        )
    }

    // Lê resposta como texto (alguns servidores retornam JSON com Content-Type errado)
    let payload = ''
    try {
        const text = await response.text()
        const parsed = tryParseJson(text)
        payload = parsed ?? text
    } catch {
        payload = ''
    }

    // Se request falhou, lança erro com mensagem amigável
    if (!response.ok) {
        const backendMessage = extractBackendMessage(payload)
        const statusMessage = ERROR_MESSAGES[response.status] ?? ERROR_MESSAGES.default
        throw new Error(backendMessage || statusMessage)
    }

    // Se payload é string e deve ser JSON, tenta parsear
    if (typeof payload === 'string') {
        const contentType = response.headers.get('content-type') ?? ''
        if (contentType.includes('application/json')) {
            const parsed = tryParseJson(payload)
            if (parsed !== null) return parsed
        }
    }

    return payload
}

export { API_BASE_URL, request }