// Paleta categórica validada (skill dataviz — ver references/palette.md).
// Ordem fixa, nunca ciclada por índice: a cor segue a identidade da chave
// (loteId, racaNome, grupoProdutoNome), não a posição/ordem de renderização.
const CATEGORICAL_LIGHT = [
  '#2a78d6', // 1 blue
  '#eb6834', // 2 orange
  '#1baf7a', // 3 aqua
  '#eda100', // 4 yellow
  '#e87ba4', // 5 magenta
  '#008300', // 6 green
  '#4a3aa7', // 7 violet
  '#e34948', // 8 red
]

const CATEGORICAL_DARK = [
  '#3987e5',
  '#d95926',
  '#199e70',
  '#c98500',
  '#d55181',
  '#008300',
  '#9085e9',
  '#e66767',
]

function isDarkTheme() {
  if (typeof document === 'undefined') return false
  return document.documentElement.getAttribute('data-theme') === 'dark'
}

// Hash determinístico (djb2) — mesma chave sempre cai no mesmo slot.
function hashString(str) {
  let hash = 5381
  for (let i = 0; i < str.length; i += 1) {
    hash = (hash * 33) ^ str.charCodeAt(i)
  }
  return Math.abs(hash)
}

/** Cor estável por chave (ex.: loteId, racaNome, grupoProdutoNome). */
function getCorCategoria(chave, { dark } = {}) {
  const ramp = (dark ?? isDarkTheme()) ? CATEGORICAL_DARK : CATEGORICAL_LIGHT
  const indice = hashString(String(chave ?? '')) % ramp.length
  return ramp[indice]
}

export { getCorCategoria, CATEGORICAL_LIGHT, CATEGORICAL_DARK }
