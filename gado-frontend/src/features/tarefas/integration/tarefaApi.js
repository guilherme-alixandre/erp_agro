import { request } from '../../../integration/apiClient'

function normalizeTarefa(raw) {
  return {
    id: raw?.id ?? null,
    descricao: raw?.descricao ?? '',
    dataLimite: raw?.dataLimite ?? null,
    statusConclusao: raw?.statusConclusao === true,
    atribuidoPorEmail: raw?.atribuidoPorEmail ?? '',
    atribuidoPorNome: raw?.atribuidoPorNome ?? '',
    atribuidoParaEmail: raw?.atribuidoParaEmail ?? '',
    atribuidoParaNome: raw?.atribuidoParaNome ?? '',
    createdAt: raw?.createdAt ?? null,
  }
}

async function listarMinhasTarefas() {
  const payload = await request('/tarefas')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar as tarefas.')
  }
  return payload.map(normalizeTarefa)
}

async function atribuirTarefa(dto) {
  const body = {
    descricao: dto.descricao.trim(),
    dataLimite: dto.dataLimite || null,
    atribuidoParaEmail: dto.atribuidoParaEmail,
  }
  const payload = await request('/tarefas', {
    method: 'POST',
    body: JSON.stringify(body),
  })
  return normalizeTarefa(payload)
}

async function editarTarefa(id, dto) {
  const payload = await request(`/tarefas/${id}`, {
    method: 'PUT',
    body: JSON.stringify(dto),
  })
  return normalizeTarefa(payload)
}

function concluirTarefa(id, statusConclusao = true) {
  return editarTarefa(id, { statusConclusao })
}

function excluirTarefa(id) {
  return request(`/tarefas/${id}`, { method: 'DELETE' })
}

export { listarMinhasTarefas, atribuirTarefa, editarTarefa, concluirTarefa, excluirTarefa }
