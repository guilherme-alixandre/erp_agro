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

async function testarCrudAnimal(email) {
  const emailLimpo = email.trim()
  const idTeste = `TESTE-${Date.now()}`
  const baseAnimal = {
    codigoBrinco: idTeste,
    nome: 'Animal Teste Integração',
    dataNascimento: '2020-01-01',
    pesoAtual: '350',
    raca: 'Nelore',
    cor: 'Branca',
    tamanho: 'Medio',
    sexo: 'M',
    statusAnimal: 'EX1',
  }

  const passos = []

  try {
    const created = await cadastrarAnimal(emailLimpo, baseAnimal)
    if (isBackendErrorMessage(created)) {
      throw new Error(getBackendMessage(created) || 'Falha no endpoint de criação.')
    }
    passos.push('CREATE ok')

    const loaded = await buscarAnimalPorBrinco(idTeste)
    if (!loaded || loaded.codigoBrinco !== idTeste) {
      throw new Error('Falha no endpoint de leitura.')
    }
    passos.push('READ ok')

    const updated = await atualizarAnimal(idTeste, {
      ...baseAnimal,
      nome: 'Animal Teste Integração Atualizado',
      pesoAtual: '355',
    })
    if (isBackendErrorMessage(updated)) {
      throw new Error(
        getBackendMessage(updated) || 'Falha no endpoint de atualização.',
      )
    }
    passos.push('UPDATE ok')

    const deleted = await deletarAnimal(idTeste)
    if (isBackendErrorMessage(deleted)) {
      throw new Error(getBackendMessage(deleted) || 'Falha no endpoint de exclusão.')
    }
    passos.push('DELETE ok')
  } catch (error) {
    throw new Error(error.message || 'Falha no teste de conexão CRUD.')
  } finally {
    try {
      await deletarAnimal(idTeste)
    } catch {
      // ignorado: tentativa de limpeza para não deixar dado de teste
    }
  }

  return passos
}

export {
  atualizarAnimal,
  buscarAnimalPorBrinco,
  cadastrarAnimal,
  deletarAnimal,
  getBackendMessage,
  isBackendErrorMessage,
  testarCrudAnimal,
}
