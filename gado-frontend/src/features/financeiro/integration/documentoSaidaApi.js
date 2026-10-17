import { request } from '../../../integration/apiClient'

function sanitizeText(value) {
  return String(value ?? '').trim()
}

// Autenticação agora é feita via o token JWT anexado automaticamente pelo apiClient.
function usuarioHeaders() {
  return {}
}

async function cadastrarVendaLeite(email, formData) {
  const body = {
    dataEmissao: formData.dataEmissao || null,
    numeroDocumento: sanitizeText(formData.numeroDocumento) || null,
    chaveAcesso: sanitizeText(formData.chaveAcesso) || null,
    precoLitro: formData.precoLitro !== '' && formData.precoLitro != null ? Number(formData.precoLitro) : null,
    valorTotal: formData.valorTotal !== '' && formData.valorTotal != null ? Number(formData.valorTotal) : null,
    compradorId: Number(formData.compradorId),
    itens: (formData.itens ?? []).map((item) => ({
      loteId: Number(item.loteId),
      litros: Number(item.litros),
    })),
    formaPagamento: formData.formaPagamento || null,
    dataVencimento: formData.dataVencimento || null,
    numeroParcelas: formData.formaPagamento === 'PARCELADO' && formData.numeroParcelas !== '' ? Number(formData.numeroParcelas) : null,
    valorParcela: formData.formaPagamento === 'PARCELADO' && formData.valorParcela !== '' ? Number(formData.valorParcela) : null,
  }
  return request('/documentos-saida/venda-leite', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
}

async function cadastrarVendaAnimal(email, formData) {
  const body = {
    dataEmissao: formData.dataEmissao || null,
    numeroDocumento: sanitizeText(formData.numeroDocumento) || null,
    chaveAcesso: sanitizeText(formData.chaveAcesso) || null,
    destino: formData.destino,
    animalIds: (formData.animalIds ?? []).map(Number),
    valorTotal: Number(formData.valorTotal),
    compradorId: Number(formData.compradorId),
    formaPagamento: formData.formaPagamento || null,
    dataVencimento: formData.dataVencimento || null,
    numeroParcelas: formData.formaPagamento === 'PARCELADO' && formData.numeroParcelas !== '' ? Number(formData.numeroParcelas) : null,
    valorParcela: formData.formaPagamento === 'PARCELADO' && formData.valorParcela !== '' ? Number(formData.valorParcela) : null,
  }
  return request('/documentos-saida/venda-animal', {
    method: 'POST',
    headers: usuarioHeaders(email),
    body: JSON.stringify(body),
  })
}

export { cadastrarVendaLeite, cadastrarVendaAnimal }
