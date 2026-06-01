import { request } from './apiClient'

function toSetorPayload(setor) {
  return {
    descricao: setor.descricao.trim(),
    usuario_id: setor.usuario_id,
  }
}

function getSetorDoPayload(payload) {
  if (!payload || typeof payload !== 'object') return null
  return payload.setor ?? payload.mensagem ?? payload
}

async function buscarSetorPorId(id) {
  const payload = await request(`/setores/${id}`)
  const setor = getSetorDoPayload(payload)

  if (!setor || typeof setor !== 'object' || Array.isArray(setor)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Setor não encontrado.')
  }

  return setor
}

async function buscarSetores(id, descricao, page, size) {
  const params = new URLSearchParams();
  if (id) params.append('id', id);
  if (descricao) params.append('descricao', descricao);
  if (page !== undefined) params.append('page', page);
  if (size !== undefined) params.append('size', size);

  const queryString = params.toString();
  const url = `/setores${queryString ? `?${queryString}` : ''}`;
  const payload = await request(url);

  if (!payload || (payload.Erro ?? payload.erro)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Falha ao buscar setores.');
  }

  return payload;
}

async function exportarSetoresCsv() {
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/setores/export/csv`);
  if (!response.ok) {
    throw new Error('Falha ao exportar setores para CSV.');
  }
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'setores.csv';
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
  return { message: 'Exportação CSV iniciada.' };
}

function cadastrarSetor(setor) {
  return request('/setores/', {
    method: 'POST',
    body: JSON.stringify(toSetorPayload(setor)),
  })
}

async function atualizarSetor(id, setor) {
  const payload = await request(`/setores/${id}`, {
    method: 'PUT',
    body: JSON.stringify(toSetorPayload(setor)),
  })

  if (!payload || (payload.Erro ?? payload.erro)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Falha ao atualizar setor.')
  }

  return payload
}

async function deletarSetor(id) {
  const payload = await request(`/setores/${id}`, {
    method: 'DELETE',
  })

  if (!payload || (payload.Erro ?? payload.erro)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Falha ao deletar setor.')
  }

  return payload
}

export {
  buscarSetorPorId,
  buscarSetores,
  cadastrarSetor,
  atualizarSetor,
  deletarSetor,
  exportarSetoresCsv,
  getSetorDoPayload,
}
