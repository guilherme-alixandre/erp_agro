import { useState } from 'react'
import {
  cadastrarMedicao,
  validarFormMedicao,
  labelQuantidade,
} from '../../../services/metaSetorApi'

const defaultForm = {
  loteId: '',
  dataMedicao: '',
  quantidadeLancada: '',
}

/**
 * Modal para registrar uma nova MedicaoMeta dentro de uma meta existente.
 *
 * Props:
 *  - meta        → objeto MetaSetor normalizado (id, tipoMeta, setorId)
 *  - lotes       → lista de lotes disponíveis para seleção
 *  - emailUsuario→ string com o email do usuário logado
 *  - onClose     → fn() — fecha o modal sem salvar
 *  - onSaved     → fn() — chamado após salvar com sucesso (dispara reload)
 */
function MedicaoModal({ meta, lotes, emailUsuario, onClose, onSaved }) {
  const [form, setForm] = useState(defaultForm)
  const [erros, setErros] = useState({})
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState('')

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
    if (erros[name]) {
      setErros((current) => ({ ...current, [name]: '' }))
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFeedback('')

    const novosErros = validarFormMedicao(form)
    if (Object.keys(novosErros).length > 0) {
      setErros(novosErros)
      return
    }

    setIsSaving(true)
    try {
      await cadastrarMedicao(emailUsuario, {
        metaSetorId: meta.id,
        loteId: Number(form.loteId),
        dataMedicao: form.dataMedicao,
        quantidadeLancada: Number(form.quantidadeLancada),
      })
      onSaved()
    } catch (error) {
      setFeedback(error.message || 'Falha ao salvar medição.')
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-box">
        <h2>Adicionar Medição</h2>
        <p style={{ fontSize: '0.82rem', color: '#6b7280', marginTop: '-1rem', marginBottom: '1.25rem' }}>
          Meta: <strong>{meta.setorNome}</strong> — {meta.tipoMeta === 'LEITE' ? 'Leite' : 'Arroba'}
        </p>

        <form className="meta-form" onSubmit={handleSubmit} noValidate>

          {/* Lote */}
          <div className="form-group">
            <label>
              <span>Lote</span>
              <select
                name="loteId"
                value={form.loteId}
                onChange={handleChange}
                className={erros.loteId ? 'field-error' : ''}
              >
                <option value="">Selecione um lote...</option>
                {lotes.map((lote) => (
                  <option key={lote.id} value={lote.id}>
                    {lote.descricao}
                  </option>
                ))}
              </select>
              {erros.loteId && (
                <span className="field-error-msg">{erros.loteId}</span>
              )}
            </label>
          </div>

          {/* Data da medição */}
          <div className="form-group">
            <label>
              <span>Data da Medição</span>
              <input
                type="date"
                name="dataMedicao"
                value={form.dataMedicao}
                onChange={handleChange}
                className={erros.dataMedicao ? 'field-error' : ''}
                max={new Date().toISOString().slice(0, 10)}
              />
              {erros.dataMedicao && (
                <span className="field-error-msg">{erros.dataMedicao}</span>
              )}
            </label>
          </div>

          {/* Quantidade */}
          <div className="form-group">
            <label>
              <span>{labelQuantidade(meta.tipoMeta)}</span>
              <input
                type="number"
                name="quantidadeLancada"
                value={form.quantidadeLancada}
                onChange={handleChange}
                min="0.01"
                step="0.01"
                placeholder="0,00"
                className={erros.quantidadeLancada ? 'field-error' : ''}
              />
              {erros.quantidadeLancada && (
                <span className="field-error-msg">{erros.quantidadeLancada}</span>
              )}
            </label>
          </div>

          {feedback && (
            <p className="feedback feedback--error">{feedback}</p>
          )}

          <div className="modal-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={onClose}
              disabled={isSaving}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn-primary"
              disabled={isSaving}
            >
              {isSaving ? 'Salvando...' : 'Salvar Medição'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default MedicaoModal
