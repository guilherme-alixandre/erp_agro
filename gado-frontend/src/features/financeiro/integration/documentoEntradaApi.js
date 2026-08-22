import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

function usuarioHeaders(email) {
  const emailLimpo = sanitizeText(email)
  return emailLimpo ? { 'X-Usuario-Email': emailLimpo } : {}
}

function normalizeItem(raw) {
  return {
    id: raw?.id ?? null,
    descricaoXml: raw?.descricaoXml ?? '',
    codigoXml: raw?.codigoXml ?? '',
    produtoId: raw?.produtoId ?? null,
    produtoNome: raw?.produtoNome ?? '',
    quantidade: raw?.quantidade ?? 0,
    valorUnitario: raw?.valorUnitario ?? 0,
    valorTotal: raw?.valorTotal ?? 0,
    vinculado: raw?.vinculado === true,
    vinculadoPorEmail: raw?.vinculadoPorEmail ?? '',
    vinculadoEm: raw?.vinculadoEm ?? null,
    naturezaFinanceira: raw?.naturezaFinanceira ?? null,
  }
}

function normalizeDocumento(raw) {
  return {
    id: raw?.id ?? null,
    tipoDocumento: raw?.tipoDocumento ?? '',
    statusAprovacao: raw?.statusAprovacao ?? '',
    numeroDocumento: raw?.numeroDocumento ?? '',
    serie: raw?.serie ?? '',
    chaveAcessoNfe: raw?.chaveAcessoNfe ?? '',
    dataEmissao: raw?.dataEmissao ?? null,
    dataEntrada: raw?.dataEntrada ?? null,
    fornecedorId: raw?.fornecedorId ?? null,
    fornecedorNome: raw?.fornecedorNome ?? '',
    valorTotal: raw?.valorTotal ?? 0,
    justificativaRecusa: raw?.justificativaRecusa ?? '',
    criadoPorEmail: raw?.criadoPorEmail ?? '',
    aprovadoPorEmail: raw?.aprovadoPorEmail ?? '',
    aprovadoEm: raw?.aprovadoEm ?? null,
    ultimaEdicaoPorEmail: raw?.ultimaEdicaoPorEmail ?? '',
    ultimaEdicaoEm: raw?.ultimaEdicaoEm ?? null,
    itens: Array.isArray(raw?.itens) ? raw.itens.map(normalizeItem) : [],
  }
}

// ── Leitura ──────────────────────────────────────────────────────────────

async function listarDocumentos(email) {
  const payload = await request('/documentos-entrada', { headers: usuarioHeaders(email) })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar os documentos de entrada.')
  }
  return payload.map(normalizeDocumento)
}

async function listarPendentesAprovacao(email) {
  const payload = await request('/documentos-entrada/pendentes', { headers: usuarioHeaders(email) })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar os documentos pendentes.')
  }
  return payload.map(normalizeDocumento)
}

// ── Importação / cadastro ──────────────────────────────────────────────

async function importarNfeXml(email, file) {
  const formData = new FormData()
  formData.append('file', file)
  const payload = await request('/documentos-entrada/importar-nfe', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: formData,
  })
  return normalizeDocumento(payload)
}

async function cadastrarReciboSimples(email, formData) {
  const body = {
    descricao: sanitizeText(formData.descricao),
    dataEmissao: formData.dataEmissao || null,
    fornecedorId: formData.fornecedorId ? Number(formData.fornecedorId) : null,
    produtoId: Number(formData.produtoId),
    quantidade: Number(formData.quantidade),
    valorTotal: Number(formData.valorTotal),
    naturezaFinanceira: formData.naturezaFinanceira,
  }
  const payload = await request('/documentos-entrada/recibos', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeDocumento(payload)
}

async function excluirDocumento(id, email) {
  return request(`/documentos-entrada/${id}`, {
    method: 'DELETE',
    headers: usuarioHeaders(email),
  })
}

// ── Aprovação ────────────────────────────────────────────────────────────

async function aprovarDocumento(id, email) {
  const payload = await request(`/documentos-entrada/${id}/aprovar`, {
    method: 'POST',
    headers: usuarioHeaders(email),
  })
  return normalizeDocumento(payload)
}

async function recusarDocumento(id, email, justificativa) {
  const payload = await request(`/documentos-entrada/${id}/recusar`, {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify({ justificativa }),
  })
  return normalizeDocumento(payload)
}

// ── Edição segura (dupla validação por senha) ─────────────────────────────

async function editarNfe(id, email, formData) {
  const body = {
    numeroDocumento: formData.numeroDocumento ? sanitizeText(formData.numeroDocumento) : undefined,
    serie: formData.serie ? sanitizeText(formData.serie) : undefined,
    dataEmissao: formData.dataEmissao || undefined,
    fornecedorId: formData.fornecedorId ? Number(formData.fornecedorId) : undefined,
    valorTotal: formData.valorTotal !== '' && formData.valorTotal != null ? Number(formData.valorTotal) : undefined,
    itens: Array.isArray(formData.itens) && formData.itens.length > 0
      ? formData.itens.map((item) => ({
          itemId: Number(item.itemId),
          quantidade: item.quantidade !== '' && item.quantidade != null ? Number(item.quantidade) : undefined,
          valorUnitario: item.valorUnitario !== '' && item.valorUnitario != null ? Number(item.valorUnitario) : undefined,
          valorTotal: item.valorTotal !== '' && item.valorTotal != null ? Number(item.valorTotal) : undefined,
        }))
      : undefined,
    senhaConfirmacao: formData.senhaConfirmacao,
  }
  const payload = await request(`/documentos-entrada/${id}/nfe`, {
    method: 'PUT',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
  return normalizeDocumento(payload)
}

// ── Vinculação de item ao catálogo ────────────────────────────────────────

async function vincularProduto(idItem, email, produtoId) {
  const payload = await request(`/documentos-entrada/itens/${idItem}/vincular`, {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify({ produtoId: Number(produtoId) }),
  })
  return normalizeItem(payload)
}

export {
  listarDocumentos,
  listarPendentesAprovacao,
  importarNfeXml,
  cadastrarReciboSimples,
  aprovarDocumento,
  recusarDocumento,
  editarNfe,
  vincularProduto,
  excluirDocumento,
}
