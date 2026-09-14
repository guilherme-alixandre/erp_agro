const moedaBRL = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/** Valor monetário no padrão brasileiro (R$ 1.234,56). */
function formatarMoeda(valor) {
  const numero = Number(valor)
  return moedaBRL.format(Number.isFinite(numero) ? numero : 0)
}

/**
 * Datas sem horário: LocalDate ("2026-08-20") ou java.util.Date gravado à meia-noite UTC
 * ("2026-08-20T00:00:00.000Z"). Usa só a parte AAAA-MM-DD, porque converter com `new Date`
 * mostraria o dia anterior nos fusos do Brasil.
 */
function formatarData(valor) {
  if (!valor) return '—'
  const partes = /^(\d{4})-(\d{2})-(\d{2})/.exec(String(valor))
  return partes ? `${partes[3]}/${partes[2]}/${partes[1]}` : String(valor)
}

/** Data e hora no fuso local (LocalDateTime ou instantes com fuso). */
function formatarDataHora(valor) {
  if (!valor) return '—'
  const data = new Date(valor)
  if (Number.isNaN(data.getTime())) return String(valor)
  return data.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

/** Data de hoje no fuso local, no formato aceito por <input type="date">. */
function hojeIso() {
  const agora = new Date()
  const mes = String(agora.getMonth() + 1).padStart(2, '0')
  const dia = String(agora.getDate()).padStart(2, '0')
  return `${agora.getFullYear()}-${mes}-${dia}`
}

export { formatarData, formatarDataHora, formatarMoeda, hojeIso }
