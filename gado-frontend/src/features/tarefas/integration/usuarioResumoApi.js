import { request } from '../../../integration/apiClient'

async function listarUsuariosResumo() {
  const payload = await request('/usuarios/resumo')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar usuários.')
  }
  return payload
}

export { listarUsuariosResumo }
