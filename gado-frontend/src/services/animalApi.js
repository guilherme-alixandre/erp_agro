import { request } from './apiClient'

function toPayload(animal) {
  const payload = {
    codigoBrinco: animal.codigoBrinco.trim(),
    nome: animal.nome.trim(),
    dataNascimento: `${animal.dataNascimento}T00:00:00`,
    pesoAtual: Number(animal.pesoAtual),
    raca: animal.raca.trim(),
    cor: animal.cor.trim(),
    tamanho: animal.tamanho.trim(),
    sexo: animal.sexo,
    statusAnimal: animal.statusAnimal,
  }

  return payload
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
    tamanho: rawAnimal?.tamanho ?? '',
    sexo: rawAnimal?.sexo ?? 'M',
    statusAnimal: rawAnimal?.statusAnimal ?? 'EX1',
    vacinas: rawAnimal?.vacinas ?? '',
  }
}

function isBackendErrorMessage(message) {
  if (typeof message !== 'string') {
    return false
  }

  const normalized = message.toLowerCase()
  return normalized.includes('erro') || normalized.includes('não encontrado')
}

function cadastrarAnimal(email, animal) {
  const emailCodificado = encodeURIComponent(email)
  return request(`/animais/usuarios/${emailCodificado}`, {
    method: 'POST',
    body: JSON.stringify(toPayload(animal)),
  })
}

async function buscarAnimalPorBrinco(brinco) {
  const codigoBrinco = encodeURIComponent(brinco.trim())
  const payload = await request(`/animais/${codigoBrinco}`)
  const mensagem = payload?.mensagem

  if (!mensagem || typeof mensagem === 'string') {
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
  isBackendErrorMessage,
}
