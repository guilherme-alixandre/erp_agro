import { request } from './apiClient'

/**
 * Cadastra um novo lote no sistema.
 * @param {Object} lote - Objeto contendo descricao, racaPredominante e usuario_id.
 * @returns {Promise<string>} Mensagem de sucesso ou erro do backend.
 */
function cadastrarLote(lote) {
  return request('/lotes/', {
    method: 'POST',
    body: JSON.stringify({
      descricao: lote.descricao.trim(),
      racaPredominante: lote.racaPredominante.trim(),
      usuario_id: Number(lote.usuario_id),
    }),
  })
}

/**
 * Busca um lote pelo ID.
 * @param {number|string} id - ID do lote.
 * @returns {Promise<Object>} Dados do lote.
 */
function buscarLotePorId(id) {
  return request(`/lotes/${id}`)
}

/**
 * Atualiza um lote existente.
 * @param {number|string} id - ID do lote.
 * @param {Object} lote - Objeto contendo descricao e racaPredominante.
 * @returns {Promise<string>} Mensagem de sucesso ou erro do backend.
 */
function atualizarLote(id, lote) {
  return request(`/lotes/${id}`, {
    method: 'PUT',
    body: JSON.stringify({
      descricao: lote.descricao.trim(),
      racaPredominante: lote.racaPredominante.trim(),
    }),
  })
}

/**
 * Deleta um lote pelo ID.
 * @param {number|string} id - ID do lote.
 * @returns {Promise<string>} Mensagem de sucesso ou erro do backend.
 */
function deletarLote(id) {
  return request(`/lotes/${id}`, {
    method: 'DELETE',
  })
}

export {
  cadastrarLote,
  buscarLotePorId,
  atualizarLote,
  deletarLote,
}
