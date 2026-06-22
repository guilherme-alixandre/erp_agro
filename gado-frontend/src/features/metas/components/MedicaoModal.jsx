import { useState } from 'react'
import {
  cadastrarMedicao,
  atualizarMedicao,
  validarFormMedicao,
  labelQuantidade,
} from '../../../services/metaSetorApi'
import SearchSelectModal from '../../../components/shared/SearchSelectModal'

function RequiredLabel({ children }) {
  return (
    <span>
      {children} <span className="required-marker" aria-hidden="true">*</span>
    </span>
  )
}

const LOTE_COLUMNS = [
  { key: 'codigo', label: 'Código' },
  { key: 'descricao', label: 'Descrição' },
  { key: 'totalAnimais', label: 'Animais' },
]

const defaultForm = {
  loteId: '',
  dataMedicao: '',
  quantidadeLancada: '',
}

/**
 * Modal para registrar ou editar uma MedicaoMeta.
 *
 * Props:
 *  - meta               → objeto MetaSetor normalizado (id, tipoMeta, setorNome)
 *  - lotes              → lista de lotes disponíveis para seleção (normalizeLote[])
 *  - emailUsuario       → string com o email do usuário logado
 *  - medicaoParaEditar  → objeto de medição existente (opcional — ausente = modo criação)
 *  - onClose            → fn() — fecha o modal sem salvar
 *  - onSaved            → fn() — chamado após salvar com sucesso (dispara reload)
 */
function MedicaoModal({ meta, lotes, emailUsuario, medicaoParaEditar, onClose, onSaved }) {
  const modoEdicao = Boolean(medicaoParaEditar)

  const [form, setForm] = useState(
    modoEdicao
      ? {
          loteId: String(medicaoParaEditar.loteId),
          dataMedicao: medicaoParaEditar.dataMedicao,
          quantidadeLancada: String(medicaoParaEditar.quantidadeLancada),
        }
      : defaultForm,
  )
  const [erros, setErros] = useState({})
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState('')
  const [loteModalOpen, setLoteModalOpen] = useState(false)

  const loteSelecionado = form.loteId
    ? lotes.find((l) => String(l.id) === form.loteId) ?? null
    : null

  const loteTriggerText = loteSelecionado
    ? `${loteSelecionado.codigo}${loteSelecionado.descricao ? ` — ${loteSelecionado.descricao}` : ''}`
    : null

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
    if (erros[name]) {
      setErros((current) => ({ ...current, [name]: '' }))
    }
  }

  function handleLoteConfirm(ids) {
    const id = ids[0] ?? null
    setForm((current) => ({ ...current, loteId: id !== null ? String(id) : '' }))
    if (erros.loteId) {
      setErros((current) => ({ ...current, loteId: '' }))
    }
    setLoteModalOpen(false)
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
      if (modoEdicao) {
        await atualizarMedicao(emailUsuario, medicaoParaEditar.id, {
          loteId: Number(form.loteId),
          dataMedicao: form.dataMedicao,
          quantidadeLancada: Number(form.quantidadeLancada),
        })
      } else {
        await cadastrarMedicao(emailUsuario, {
          metaSetorId: meta.id,
          loteId: Number(form.loteId),
          dataMedicao: form.dataMedicao,
          quantidadeLancada: Number(form.quantidadeLancada),
        })
      }
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
        <h2>{modoEdicao ? 'Editar Medição' : 'Adicionar Medição'}</h2>
        <p
          style={{
            fontSize: '0.82rem',
            color: '#6b7280',
            marginTop: '-1rem',
            marginBottom: '1.25rem',
          }}
        >
          Meta: <strong>{meta.setorNome}</strong> —{' '}
          {meta.tipoMeta === 'LEITE' ? 'Leite' : 'Arroba'}
        </p>

        <form className="meta-form" onSubmit={handleSubmit} noValidate>

          {/* Lote */}
          <div className="form-group">
            <label>
              <RequiredLabel>Lote</RequiredLabel>
              <button
                type="button"
                className={`ssm-trigger${erros.loteId ? ' ssm-trigger--error' : ''}`}
                onClick={() => setLoteModalOpen(true)}
              >
                <span className={loteTriggerText ? '' : 'ssm-trigger__placeholder'}>
                  {loteTriggerText ?? 'Selecione um lote...'}
                </span>
                <span className="ssm-trigger__arrow" aria-hidden="true">
                  ▼
                </span>
              </button>
              {erros.loteId ? (
                <span className="field-error-msg">{erros.loteId}</span>
              ) : null}
            </label>
          </div>

          {/* Data da medição */}
          <div className="form-group">
            <label>
              <RequiredLabel>Data da Medição</RequiredLabel>
              <input
                type="date"
                name="dataMedicao"
                value={form.dataMedicao}
                onChange={handleChange}
                className={erros.dataMedicao ? 'field-error' : ''}
                max={new Date().toISOString().slice(0, 10)}
              />
              {erros.dataMedicao ? (
                <span className="field-error-msg">{erros.dataMedicao}</span>
              ) : null}
            </label>
          </div>

          {/* Quantidade */}
          <div className="form-group">
            <label>
              <RequiredLabel>{labelQuantidade(meta.tipoMeta)}</RequiredLabel>
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
              {erros.quantidadeLancada ? (
                <span className="field-error-msg">{erros.quantidadeLancada}</span>
              ) : null}
            </label>
          </div>

          {feedback ? (
            <p className="feedback feedback--error">{feedback}</p>
          ) : null}

          <div className="modal-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={onClose}
              disabled={isSaving}
            >
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving
                ? 'Salvando...'
                : modoEdicao
                  ? 'Salvar Alterações'
                  : 'Salvar Medição'}
            </button>
          </div>
        </form>
      </div>

      {loteModalOpen ? (
        <SearchSelectModal
          title="Selecionar lote"
          items={lotes}
          selectedIds={form.loteId ? [Number(form.loteId)] : []}
          onConfirm={handleLoteConfirm}
          onClose={() => setLoteModalOpen(false)}
          multiSelect={false}
          columns={LOTE_COLUMNS}
        />
      ) : null}
    </div>
  )
}

export default MedicaoModal
