import { useMemo, useState } from 'react'
import SearchSelectModal from '../../../components/shared/SearchSelectModal'

const ANIMAL_COLUMNS = [
  { key: 'codigoBrinco', label: 'Código' },
  { key: 'racaNome', label: 'Raça' },
]

const STATUS_VENDAVEL = new Set(['ATIVO', 'OBSERVACAO'])

function todayIso() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

function RegistrarVendaModal({ lotes, animaisDisponiveis, isSaving, feedback, onClose, onSubmitLeite, onSubmitAnimal }) {
  const [tipo, setTipo] = useState('')

  // ── Venda de leite ───────────────────────────────────────────────────
  const [leiteForm, setLeiteForm] = useState({
    dataEmissao: todayIso(),
    numeroDocumento: '',
    chaveAcesso: '',
    precoLitro: '',
    valorTotal: '',
    itens: [{ loteId: '', litros: '' }],
  })

  function handleLeiteChange(event) {
    const { name, value } = event.target
    setLeiteForm((c) => ({ ...c, [name]: value }))
  }

  function handleLeiteItemChange(index, field, value) {
    setLeiteForm((c) => ({
      ...c,
      itens: c.itens.map((item, i) => (i === index ? { ...item, [field]: value } : item)),
    }))
  }

  function addLeiteItem() {
    setLeiteForm((c) => ({ ...c, itens: [...c.itens, { loteId: '', litros: '' }] }))
  }

  function removeLeiteItem(index) {
    setLeiteForm((c) => ({
      ...c,
      itens: c.itens.length > 1 ? c.itens.filter((_, i) => i !== index) : c.itens,
    }))
  }

  function handleSubmitLeiteForm(event) {
    event.preventDefault()
    onSubmitLeite(leiteForm)
  }

  // ── Venda / abate de animal ──────────────────────────────────────────
  const animaisVendaveis = useMemo(
    () => animaisDisponiveis.filter((a) => STATUS_VENDAVEL.has(a.statusAnimal)),
    [animaisDisponiveis],
  )

  const [animalForm, setAnimalForm] = useState({
    dataEmissao: todayIso(),
    numeroDocumento: '',
    chaveAcesso: '',
    destino: 'VENDIDO',
    modoSelecao: 'UNICO',
    loteId: '',
    animalIds: [],
    valorTotal: '',
  })
  const [selecaoModalOpen, setSelecaoModalOpen] = useState(false)

  function handleAnimalChange(event) {
    const { name, value } = event.target
    setAnimalForm((c) => {
      const next = { ...c, [name]: value }
      if (name === 'modoSelecao') {
        next.loteId = ''
        next.animalIds = []
      }
      return next
    })
  }

  function handleLoteSelecionado(event) {
    const loteId = event.target.value
    const lote = lotes.find((l) => String(l.id) === String(loteId))
    const idsDoLote = lote
      ? lote.alocacoes.flatMap((aloc) => aloc.animais.map((a) => a.id))
      : []
    const idsVendaveis = idsDoLote.filter((id) => animaisVendaveis.some((a) => a.id === id))
    setAnimalForm((c) => ({ ...c, loteId, animalIds: idsVendaveis }))
  }

  const itensParaSelecao = animalForm.modoSelecao === 'LOTE' && animalForm.loteId
    ? animaisVendaveis.filter((a) => {
        const lote = lotes.find((l) => String(l.id) === String(animalForm.loteId))
        const idsDoLote = lote ? lote.alocacoes.flatMap((aloc) => aloc.animais.map((x) => x.id)) : []
        return idsDoLote.includes(a.id)
      })
    : animaisVendaveis

  function handleSubmitAnimalForm(event) {
    event.preventDefault()
    onSubmitAnimal(animalForm)
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>Registrar venda</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        {!tipo ? (
          <div className="venda-tipo-escolha">
            <p className="form-help">O que está sendo vendido?</p>
            <div className="modal-actions">
              <button type="button" className="btn-primary" onClick={() => setTipo('LEITE')}>
                🥛 Venda de leite
              </button>
              <button type="button" className="btn-primary" onClick={() => setTipo('ANIMAL')}>
                🐄 Venda / abate de animais
              </button>
            </div>
          </div>
        ) : null}

        {tipo === 'LEITE' ? (
          <form className="animal-form" onSubmit={handleSubmitLeiteForm}>
            <div className="consumo-estoque__itens">
              {leiteForm.itens.map((item, index) => (
                <div className="consumo-estoque__item-row" key={index}>
                  <label className="consumo-estoque__item-produto">
                    <span>Lote de origem</span>
                    <select
                      value={item.loteId}
                      onChange={(e) => handleLeiteItemChange(index, 'loteId', e.target.value)}
                      required
                    >
                      <option value="">Selecione...</option>
                      {lotes.map((l) => (
                        <option key={l.id} value={l.id}>
                          {l.codigo}{l.descricao ? ` — ${l.descricao}` : ''}
                        </option>
                      ))}
                    </select>
                  </label>
                  <label className="consumo-estoque__item-quantidade">
                    <span>Litros vendidos</span>
                    <input
                      type="number"
                      value={item.litros}
                      onChange={(e) => handleLeiteItemChange(index, 'litros', e.target.value)}
                      min="0.01"
                      step="0.01"
                      required
                    />
                  </label>
                  <button
                    type="button"
                    className="btn-icon btn-icon--danger consumo-estoque__remove-item"
                    onClick={() => removeLeiteItem(index)}
                    disabled={leiteForm.itens.length === 1}
                    aria-label="Remover lote"
                  >
                    ✕
                  </button>
                </div>
              ))}
              <button type="button" className="btn-secondary consumo-estoque__add-item" onClick={addLeiteItem}>
                + Adicionar lote
              </button>
            </div>

            <label>
              <span>Data da venda <span className="required-marker">*</span></span>
              <input type="date" name="dataEmissao" value={leiteForm.dataEmissao} onChange={handleLeiteChange} required />
            </label>

            <label>
              <span>Preço por litro (R$)</span>
              <input type="number" name="precoLitro" value={leiteForm.precoLitro} onChange={handleLeiteChange} min="0" step="0.01" />
            </label>
            <p className="form-help">Informe o preço por litro OU o valor final de venda abaixo.</p>

            <label>
              <span>Valor final de venda (R$)</span>
              <input type="number" name="valorTotal" value={leiteForm.valorTotal} onChange={handleLeiteChange} min="0" step="0.01" />
            </label>

            <label>
              <span>Número da NF (opcional)</span>
              <input type="text" name="numeroDocumento" value={leiteForm.numeroDocumento} onChange={handleLeiteChange} />
            </label>

            <label>
              <span>Chave de acesso (opcional)</span>
              <input type="text" name="chaveAcesso" value={leiteForm.chaveAcesso} onChange={handleLeiteChange} maxLength={44} />
            </label>

            {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={() => setTipo('')} disabled={isSaving}>
                Voltar
              </button>
              <button type="submit" className="btn-primary" disabled={isSaving}>
                {isSaving ? 'Registrando...' : 'Registrar venda de leite'}
              </button>
            </div>
          </form>
        ) : null}

        {tipo === 'ANIMAL' ? (
          <form className="animal-form" onSubmit={handleSubmitAnimalForm}>
            <label>
              <span>Destino <span className="required-marker">*</span></span>
              <select name="destino" value={animalForm.destino} onChange={handleAnimalChange} required>
                <option value="VENDIDO">Vivo (vendido)</option>
                <option value="ABATIDO">Abatido</option>
              </select>
            </label>

            <label>
              <span>Seleção <span className="required-marker">*</span></span>
              <select name="modoSelecao" value={animalForm.modoSelecao} onChange={handleAnimalChange}>
                <option value="UNICO">Um único animal</option>
                <option value="LOTE">Um lote inteiro</option>
              </select>
            </label>

            {animalForm.modoSelecao === 'LOTE' ? (
              <label>
                <span>Lote <span className="required-marker">*</span></span>
                <select value={animalForm.loteId} onChange={handleLoteSelecionado} required>
                  <option value="">Selecione o lote...</option>
                  {lotes.map((l) => (
                    <option key={l.id} value={l.id}>{l.codigo}{l.descricao ? ` — ${l.descricao}` : ''}</option>
                  ))}
                </select>
              </label>
            ) : null}

            <div className="setores-fieldset__select">
              <span>Animais selecionados</span>
              <button type="button" className="ssm-trigger" onClick={() => setSelecaoModalOpen(true)}>
                <span className={animalForm.animalIds.length ? '' : 'ssm-trigger__placeholder'}>
                  {animalForm.animalIds.length > 0
                    ? `${animalForm.animalIds.length} animal(is) selecionado(s)`
                    : 'Selecionar animais...'}
                </span>
                <span className="ssm-trigger__arrow" aria-hidden="true">▼</span>
              </button>
            </div>

            <label>
              <span>Data da venda <span className="required-marker">*</span></span>
              <input type="date" name="dataEmissao" value={animalForm.dataEmissao} onChange={handleAnimalChange} required />
            </label>

            <label>
              <span>Valor total da venda (R$) <span className="required-marker">*</span></span>
              <input type="number" name="valorTotal" value={animalForm.valorTotal} onChange={handleAnimalChange} min="0.01" step="0.01" required />
            </label>
            <p className="form-help">Dividido igualmente entre os animais selecionados.</p>

            <label>
              <span>Número da NF (opcional)</span>
              <input type="text" name="numeroDocumento" value={animalForm.numeroDocumento} onChange={handleAnimalChange} />
            </label>

            <label>
              <span>Chave de acesso (opcional)</span>
              <input type="text" name="chaveAcesso" value={animalForm.chaveAcesso} onChange={handleAnimalChange} maxLength={44} />
            </label>

            {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={() => setTipo('')} disabled={isSaving}>
                Voltar
              </button>
              <button type="submit" className="btn-primary" disabled={isSaving || animalForm.animalIds.length === 0}>
                {isSaving ? 'Registrando...' : 'Registrar venda/abate'}
              </button>
            </div>
          </form>
        ) : null}

        {!tipo ? (
          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
          </div>
        ) : null}
      </div>

      {selecaoModalOpen ? (
        <SearchSelectModal
          title="Selecionar animais"
          items={itensParaSelecao}
          selectedIds={animalForm.animalIds}
          onConfirm={(ids) => {
            setAnimalForm((c) => ({ ...c, animalIds: ids }))
            setSelecaoModalOpen(false)
          }}
          onClose={() => setSelecaoModalOpen(false)}
          multiSelect={animalForm.modoSelecao === 'LOTE'}
          columns={ANIMAL_COLUMNS}
        />
      ) : null}
    </div>
  )
}

export default RegistrarVendaModal
