import { request } from './apiClient'

const MIN_BIRTH_YEAR = 1990

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function parseOptionalNumber(value, fieldLabel) {
  const raw = String(value ?? '').replace(',', '.').trim()
  if (!raw) return null

  const parsed = Number(raw)
  if (!Number.isFinite(parsed) || parsed < 0) {
    throw new Error(`Informe um valor válido para ${fieldLabel} (número maior ou igual a zero).`)
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
    throw new Error(`A data de nascimento deve ter ano entre ${MIN_BIRTH_YEAR} e ${currentYear}.`)
  }

  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const birth = new Date(year, month - 1, day)
  if (birth.getTime() > today.getTime()) {
    throw new Error('Data de nascimento não pode ser no futuro.')
  }
}

function toPayload(animal) {
  const dataNascimento = sanitizeText(animal.dataNascimento)
  validateBirthDate(dataNascimento)

  const pesoAtualRaw = String(animal.pesoAtual ?? '').replace(',', '.')
  const pesoAtual = Number(pesoAtualRaw)

  if (!Number.isFinite(pesoAtual) || pesoAtual < 0) {
    throw new Error('Informe um peso valido (numero maior ou igual a zero).')
  }

  const payload = {
    codigoBrinco: sanitizeText(animal.codigoBrinco),
    nome: sanitizeText(animal.nome),
    dataNascimento: `${dataNascimento}T00:00:00`,
    pesoAtual,
    raca: sanitizeText(animal.raca),
    cor: sanitizeText(animal.cor),
    alturaCernelha: parseOptionalNumber(animal.alturaCernelha, 'Altura na cernelha'),
    perimetroToracico: parseOptionalNumber(animal.perimetroToracico, 'Perímetro torácico'),
    comprimentoCorporal: parseOptionalNumber(animal.comprimentoCorporal, 'Comprimento corporal'),
    sexo: animal.sexo,
    statusAnimal: animal.statusAnimal,
  }

  return payload
}

function toFormString(value) {
  if (value === null || value === undefined || value === '') return ''
  return String(value)
}

function normalizeAnimal(rawAnimal) {
  const nascimento = rawAnimal?.dataNascimento?.slice(0, 10) ?? ''
  return {
    codigoBrinco: rawAnimal?.codigoBrinco ?? '',
    nome: rawAnimal?.nome ?? '',
    dataNascimento: nascimento,
    pesoAtual:
      typeof rawAnimal?.pesoAtual === 'number'
        ? rawAnimal.pesoAtual
        : Number(rawAnimal?.pesoAtual ?? 0),
    raca: rawAnimal?.raca ?? '',
    cor: rawAnimal?.cor ?? '',
    alturaCernelha: toFormString(rawAnimal?.alturaCernelha),
    perimetroToracico: toFormString(rawAnimal?.perimetroToracico),
    comprimentoCorporal: toFormString(rawAnimal?.comprimentoCorporal),
    sexo: rawAnimal?.sexo ?? 'M',
    statusAnimal: rawAnimal?.statusAnimal ?? 'ATIVO',
    vacinas: rawAnimal?.vacinas ?? '',
  }
}

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
  if (!message) {
    return false
  }

  const normalized = String(message).toLowerCase()
  return normalized.includes('erro') || normalized.includes('não encontrado')
}

function cadastrarAnimal(email, animal) {
  const emailLimpo = sanitizeText(email)
  if (!emailLimpo) {
    throw new Error('Informe o e-mail do usuario.')
  }

  const emailCodificado = encodeURIComponent(emailLimpo)
  return request(`/animais/usuarios/${emailCodificado}`, {
    method: 'POST',
    body: JSON.stringify(toPayload(animal)),
  })
}

async function buscarAnimalPorBrinco(brinco) {
  const codigoBrinco = encodeURIComponent(brinco.trim())
  const payload = await request(`/animais/${codigoBrinco}`)
  const mensagem = payload?.mensagem

  if (!mensagem || typeof mensagem !== 'object' || Array.isArray(mensagem)) {
    throw new Error(
      typeof mensagem === 'string'
        ? mensagem
        : 'Animal não encontrado para o brinco informado.',
    )
  }

  return normalizeAnimal(mensagem)
}

function atualizarAnimal(brinco, animal) {
  const codigoBrinco = encodeURIComponent(brinco.trim())
  return request(`/animais/${codigoBrinco}`, {
    method: 'PUT',
    body: JSON.stringify(toPayload(animal)),
  })
}

function deletarAnimal(brinco) {
  const codigoBrinco = encodeURIComponent(brinco.trim())
  return request(`/animais/${codigoBrinco}`, {
    method: 'DELETE',
  })
}

export {
  atualizarAnimal,
  buscarAnimalPorBrinco,
  cadastrarAnimal,
  deletarAnimal,
  getBackendMessage,
  isBackendErrorMessage,
}
