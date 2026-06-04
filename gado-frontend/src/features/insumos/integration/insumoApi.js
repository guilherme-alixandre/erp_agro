import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function normalizeVacina(raw) {
  return {
    id: raw?.id ?? null,
    nome: raw?.nome ?? '',
    pendente: raw?.pendente === true,
  }
}

async function listarVacinas(termo) {
  const limpo = sanitizeText(termo)
  const query = limpo ? `?busca=${encodeURIComponent(limpo)}` : ''
  const payload = await request(`/insumos/vacinas${query}`)
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar vacinas.')
  }
  return payload.map(normalizeVacina)
}

async function cadastrarVacina({ nome, pendente = false }) {
  const nomeLimpo = sanitizeText(nome)
  if (!nomeLimpo) {
    throw new Error('Informe o nome da vacina.')
  }
  const payload = await request('/insumos/vacinas', {
    method: 'POST',
    body: JSON.stringify({ nome: nomeLimpo, pendente }),
  })
  return normalizeVacina(payload)
}

async function atualizarVacina(id, { nome, pendente }) {
  if (!id) {
    throw new Error('Vacina sem identificador.')
  }
  const body = {}
  if (typeof nome === 'string') {
    const nomeLimpo = sanitizeText(nome)
    if (!nomeLimpo) {
      throw new Error('Informe o nome da vacina.')
    }
    body.nome = nomeLimpo
  }
  if (typeof pendente === 'boolean') {
    body.pendente = pendente
  }
  const payload = await request(`/insumos/vacinas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body),
  })
  return normalizeVacina(payload)
}

function confirmarVacina(id) {
  return atualizarVacina(id, { pendente: false })
}

function deletarVacina(id) {
  if (!id) {
    throw new Error('Vacina sem identificador.')
  }
  return request(`/insumos/vacinas/${id}`, { method: 'DELETE' })
}

export {
  atualizarVacina,
  cadastrarVacina,
  confirmarVacina,
  deletarVacina,
  listarVacinas,
}
