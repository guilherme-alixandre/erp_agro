import { request } from './apiClient'

function toLotePayload(lote) {
  return {
    usuario_id: Number(lote.usuario_id),
    descricao: lote.descricao.trim(),
    racaPredominante: lote.racaPredominante.trim(),
  }
}

function getLoteDoPayload(payload) {
  if (!payload || typeof payload !== 'object') return null
  if (typeof payload === 'string' && !isNaN(Number(payload))) {
    return { id: Number(payload) }
  }
  return payload.lote ?? payload
}

async function buscarLotePorId(id) {
  const payload = await request(`/lotes/${id}`)
  const lote = getLoteDoPayload(payload)

  if (!lote || typeof lote !== 'object' || Array.isArray(lote)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Lote não encontrado.')
  }

  return lote
}

async function buscarLotes(id, descricao) {
  const params = new URLSearchParams();
  if (id) params.append('id', id);
  if (descricao) params.append('descricao', descricao);
  const queryString = params.toString();
  const url = `/lotes${queryString ? `?${queryString}` : ''}`;
  const payload = await request(url);
  return payload;
}

async function exportarLotesCsv() {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL ?? (import.meta.env.DEV ? '/api' : 'http://localhost:8080/api')}/lotes/export/csv`);
  if (!response.ok) {
    throw new Error('Falha ao exportar lotes para CSV.');
  }
  return response.blob();
}

function cadastrarLote(lote) {
  return request('/lotes/', {
    method: 'POST',
    body: JSON.stringify(toLotePayload(lote)),
  })
}

async function atualizarLote(id, lote) {
  const payload = await request(`/lotes/${id}`, {
    method: 'PUT',
    body: JSON.stringify({
      descricao: lote.descricao.trim(),
      racaPredominante: lote.racaPredominante.trim(),
    }),
  })

  return payload
}

async function deletarLote(id) {
  const payload = await request(`/lotes/${id}`, {
    method: 'DELETE',
  })

  return payload
}

export {
  cadastrarLote,
  buscarLotePorId,
  atualizarLote,
  deletarLote,
  getLoteDoPayload,
  buscarLotes,
  exportarLotesCsv,
}
