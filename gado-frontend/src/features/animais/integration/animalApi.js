import { request } from '../../../integration/apiClient.js'

// Configurações
const MIN_BIRTH_YEAR = 1990

function sanitizeText(value) {
    return String(value ?? '').trim()
}

function parseOptionalNumber(value, fieldLabel) {
    const raw = String(value ?? '').replace(',', '.').trim()
    if (!raw) return null

    const parsed = Number(raw)
    if (!Number.isFinite(parsed) || parsed < 0) {
        throw new Error(`Informe um valor válido para ${fieldLabel} (número ≥ zero).`)
    }
    return parsed
}

function validateBirthDate(dataNascimento) {
    if (!dataNascimento) {
        throw new Error('Informe a data de nascimento.')
    }

    const [yearStr, monthStr, dayStr] = dataNascimento.split('-')
    const year = Number(yearStr)
    const month = Number(monthStr)
    const day = Number(dayStr)

    if (!year || !month || !day) {
        throw new Error('Data de nascimento inválida.')
    }

    const currentYear = new Date().getFullYear()
    if (year < MIN_BIRTH_YEAR || year > currentYear) {
        throw new Error(`Data de nascimento deve estar entre ${MIN_BIRTH_YEAR} e ${currentYear}.`)
    }

    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const birth = new Date(year, month - 1, day)

    if (birth.getTime() > today.getTime()) {
        throw new Error('Data de nascimento não pode ser no futuro.')
    }
}

const PESO_MINIMO_KG = 30

function validatePeso(pesoAtual) {
    const peso = Number(String(pesoAtual ?? '').replace(',', '.'))
    if (!Number.isFinite(peso) || peso < PESO_MINIMO_KG) {
        throw new Error(`Informe um peso válido (mínimo de ${PESO_MINIMO_KG}kg).`)
    }
    return peso
}

// ============================================================================
// Helpers: Normalização (Backend → Frontend)
// ============================================================================

function normalizeAnimal(rawAnimal) {
    const nascimento = rawAnimal?.dataNascimento?.slice(0, 10) ?? ''

    return {
        id: rawAnimal?.id ?? null,
        codigoBrinco: rawAnimal?.codigoBrinco ?? '',
        dataNascimento: nascimento,
        pesoAtual: typeof rawAnimal?.pesoAtual === 'number'
            ? rawAnimal.pesoAtual
            : Number(rawAnimal?.pesoAtual ?? 0),
        racaId: rawAnimal?.racaId ?? '',
        racaNome: rawAnimal?.racaNome ?? '',
        racaSigla: rawAnimal?.racaSigla ?? '',
        cor: rawAnimal?.cor ?? '',
        alturaCernelha: String(rawAnimal?.alturaCernelha ?? ''),
        perimetroToracico: String(rawAnimal?.perimetroToracico ?? ''),
        comprimentoCorporal: String(rawAnimal?.comprimentoCorporal ?? ''),
        sexo: rawAnimal?.sexo ?? 'M',
        statusAnimal: rawAnimal?.statusAnimal ?? 'ATIVO',
    }
}

// ============================================================================
// Helpers: Transformação (Frontend → Backend)
// ============================================================================

function toPayload(animal) {
    const dataNascimento = sanitizeText(animal.dataNascimento)
    validateBirthDate(dataNascimento)

    const pesoAtual = validatePeso(animal.pesoAtual)

    if (!animal.racaId) {
        throw new Error('Selecione a raça do animal.')
    }

    return {
        dataNascimento: `${dataNascimento}T00:00:00`,
        pesoAtual,
        racaId: Number(animal.racaId),
        cor: sanitizeText(animal.cor),
        alturaCernelha: parseOptionalNumber(animal.alturaCernelha, 'Altura na cernelha'),
        perimetroToracico: parseOptionalNumber(animal.perimetroToracico, 'Perímetro torácico'),
        comprimentoCorporal: parseOptionalNumber(animal.comprimentoCorporal, 'Comprimento corporal'),
        sexo: animal.sexo,
        statusAnimal: animal.statusAnimal,
    }
}

// ============================================================================
// Helpers: Extração de Mensagens do Backend
// ============================================================================

function getBackendMessage(payload) {
    if (typeof payload === 'string') {
        return payload
    }

    if (payload && typeof payload === 'object') {
        const message = payload.mensagem ?? payload.message
        return typeof message === 'string' ? message : ''
    }

    return ''
}

function isBackendErrorMessage(payload) {
    const message = getBackendMessage(payload)
    if (!message) return false

    const normalized = String(message).toLowerCase()
    return normalized.includes('erro') || normalized.includes('não encontrado')
}

// ============================================================================
// API: Operações CRUD
// ============================================================================

async function buscarAnimais(termo = '') {
    const limpo = String(termo ?? '').trim()
    const query = limpo ? `?busca=${encodeURIComponent(limpo)}` : ''

    const payload = await request(`/animais${query}`)

    if (!Array.isArray(payload)) {
        throw new Error('Resposta inesperada ao buscar animais.')
    }

    return payload.map(normalizeAnimal)
}

async function buscarAnimalPorBrinco(brinco) {
    const codigoBrinco = encodeURIComponent(brinco.trim())
    const payload = await request(`/animais/${codigoBrinco}`)

    const mensagem = payload?.mensagem
    if (!mensagem || typeof mensagem !== 'object' || Array.isArray(mensagem)) {
        throw new Error(
            typeof mensagem === 'string'
                ? mensagem
                : 'Animal não encontrado para o brinco informado.'
        )
    }

    return normalizeAnimal(mensagem)
}

async function cadastrarAnimal(email, animal) {
    const emailLimpo = sanitizeText(email)
    if (!emailLimpo) {
        throw new Error('Informe o e-mail do usuário.')
    }

    const emailCodificado = encodeURIComponent(emailLimpo)
    const payload = toPayload(animal)

    return request(`/animais/usuarios/${emailCodificado}`, {
        method: 'POST',
        body: JSON.stringify(payload),
    })
}

async function atualizarAnimal(brinco, animal) {
    const codigoBrinco = encodeURIComponent(brinco.trim())
    const payload = toPayload(animal)

    return request(`/animais/${codigoBrinco}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    })
}

async function deletarAnimal(brinco) {
    const codigoBrinco = encodeURIComponent(brinco.trim())

    return request(`/animais/${codigoBrinco}`, {
        method: 'DELETE',
    })
}

// ============================================================================
// Exports
// ============================================================================

export {
    buscarAnimais,
    buscarAnimalPorBrinco,
    cadastrarAnimal,
    atualizarAnimal,
    deletarAnimal,
    getBackendMessage,
    isBackendErrorMessage,
    normalizeAnimal,
}