import { useEffect, useState } from 'react'
import {
  cadastrarMeta,
  atualizarMeta,
  validarFormMeta,
  TIPOS_GADO,
} from '../../../services/metaSetorApi'
import { useRefresh } from '../../../contexts/RefreshContext.jsx'

function RequiredLabel({ children }) {
  return (
    <span>
      {children} <span className="required-marker" aria-hidden="true">*</span>
    </span>
  )
}

const defaultForm = {
  setorId: '',
  dataInicial: '',
  dataFinal: '',
  tipoMeta: '',
  quantidadeEsperada: '',
  precoMedio: '',
  tipoGado: '',
}

/**
 * Modal de cadastro e edição de MetaSetor.
 *
 * Props:
 *  - mode         → 'create' | 'edit'
 *  - meta         → objeto MetaSetor normalizado (somente para mode='edit')
 *  - setores      → lista [{ id, nome }] para o dropdown
 *  - emailUsuario → string com o email do usuário logado
 *  - onClose      → fn() — fecha sem salvar
 *  - onSaved      → fn() — chamado após salvar com sucesso
 */
function MetaFormModal({ mode, meta, setores, emailUsuario, onClose, onSaved }) {
  const { dispararRefresh } = useRefresh()

  const [form, setForm] = useState(defaultForm)
  const [erros, setErros] = useState({})
  const [isSaving, setIsSaving] = useState(false)
  const [feedback, setFeedback] = useState('')

  // Preenche o formulário ao editar
  useEffect(() => {
    if (mode === 'edit' && meta) {
      setForm({
        setorId: String(meta.setorId ?? ''),
        dataInicial: meta.dataInicial ?? '',
        dataFinal: meta.dataFinal ?? '',
        tipoMeta: meta.tipoMeta ?? '',
        quantidadeEsperada: String(meta.quantidadeEsperada ?? ''),
        precoMedio: String(meta.precoMedio ?? ''),
        tipoGado: meta.tipoGado ?? '',
      })
    } else {
      setForm(defaultForm)
    }
  }, [mode, meta])

  function handleChange(event) {
    const { name, value } = event.target
    setForm((current) => {
      const next = { ...current, [name]: value }
      // Limpa tipoGado automaticamente ao mudar para LEITE
      if (name === 'tipoMeta' && value === 'LEITE') {
        next.tipoGado = ''
      }
      return next
    })
    if (erros[name]) {
      setErros((current) => ({ ...current, [name]: '' }))
    }
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setFeedback('')

    const novosErros = validarFormMeta(form)
    if (Object.keys(novosErros).length > 0) {
      setErros(novosErros)
      return
    }

    setIsSaving(true)
    try {
      const dto = {
        setorId: Number(form.setorId),
        dataInicial: form.dataInicial,
        dataFinal: form.dataFinal,
        tipoMeta: form.tipoMeta,
        quantidadeEsperada: Number(form.quantidadeEsperada),
        precoMedio: Number(form.precoMedio),
        tipoGado: form.tipoMeta === 'ARROBA' && form.tipoGado ? form.tipoGado : null,
      }

      if (mode === 'create') {
        await cadastrarMeta(emailUsuario, dto)
      } else {
        // No PUT o setorId e tipoMeta não são enviados (imutáveis)
        const { setorId: _s, tipoMeta: _t, ...putDto } = dto
        await atualizarMeta(meta.id, emailUsuario, putDto)
      }

      dispararRefresh()
      onSaved()
    } catch (error) {
      setFeedback(error.message || 'Falha ao salvar a meta.')
    } finally {
      setIsSaving(false)
    }
  }

  const isEdit = mode === 'edit'

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-box">
        <h2>{isEdit ? 'Editar Meta' : 'Nova Meta de Setor'}</h2>

        <form className="meta-form" onSubmit={handleSubmit} noValidate>

          {/* Setor — somente no cadastro */}
          {!isEdit && (
            <div className="form-group">
              <label>
                <RequiredLabel>Setor</RequiredLabel>
                <select
                  name="setorId"
                  value={form.setorId}
                  onChange={handleChange}
                  className={erros.setorId ? 'field-error' : ''}
                >
                  <option value="">Selecione um setor...</option>
                  {setores.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.nome}
                    </option>
                  ))}
                </select>
                {erros.setorId && (
                  <span className="field-error-msg">{erros.setorId}</span>
                )}
              </label>
            </div>
          )}

          {/* Tipo de meta — somente no cadastro */}
          {!isEdit && (
            <div className="form-group">
              <label>
                <RequiredLabel>Tipo de Meta</RequiredLabel>
                <select
                  name="tipoMeta"
                  value={form.tipoMeta}
                  onChange={handleChange}
                  className={erros.tipoMeta ? 'field-error' : ''}
                >
                  <option value="">Selecione...</option>
                  <option value="LEITE">Leite (Litros)</option>
                  <option value="ARROBA">Arroba (@)</option>
                </select>
                {erros.tipoMeta && (
                  <span className="field-error-msg">{erros.tipoMeta}</span>
                )}
              </label>
            </div>
          )}

          {/* Tipo de gado — condicional: só aparece quando tipoMeta === ARROBA */}
          {form.tipoMeta === 'ARROBA' && (
            <div className="form-group">
              <label>
                <RequiredLabel>Tipo de Gado</RequiredLabel>
                <select
                  name="tipoGado"
                  value={form.tipoGado}
                  onChange={handleChange}
                  className={erros.tipoGado ? 'field-error' : ''}
                >
                  <option value="">Selecione o tipo de gado...</option>
                  {TIPOS_GADO.map((t) => (
                    <option key={t.value} value={t.value}>
                      {t.label}
                    </option>
                  ))}
                </select>
                {erros.tipoGado && (
                  <span className="field-error-msg">{erros.tipoGado}</span>
                )}
              </label>
            </div>
          )}

          {/* Datas */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
            <div className="form-group">
              <label>
                <RequiredLabel>Data Inicial</RequiredLabel>
                <input
                  type="date"
                  name="dataInicial"
                  value={form.dataInicial}
                  onChange={handleChange}
                  className={erros.dataInicial ? 'field-error' : ''}
                />
                {erros.dataInicial && (
                  <span className="field-error-msg">{erros.dataInicial}</span>
                )}
              </label>
            </div>
            <div className="form-group">
              <label>
                <RequiredLabel>Data Final</RequiredLabel>
                <input
                  type="date"
                  name="dataFinal"
                  value={form.dataFinal}
                  onChange={handleChange}
                  min={form.dataInicial || undefined}
                  className={erros.dataFinal ? 'field-error' : ''}
                />
                {erros.dataFinal && (
                  <span className="field-error-msg">{erros.dataFinal}</span>
                )}
              </label>
            </div>
          </div>

          {/* Quantidade esperada */}
          <div className="form-group">
            <label>
              <RequiredLabel>
                {form.tipoMeta === 'LEITE'
                  ? 'Quantidade Total Esperada (Litros)'
                  : form.tipoMeta === 'ARROBA'
                  ? 'Quantidade Total Esperada (@)'
                  : 'Quantidade Total Esperada'}
              </RequiredLabel>
              <input
                type="number"
                name="quantidadeEsperada"
                value={form.quantidadeEsperada}
                onChange={handleChange}
                min="0.01"
                step="0.01"
                placeholder="0,00"
                className={erros.quantidadeEsperada ? 'field-error' : ''}
              />
              {erros.quantidadeEsperada && (
                <span className="field-error-msg">{erros.quantidadeEsperada}</span>
              )}
            </label>
          </div>

          {/* Preço médio */}
          <div className="form-group">
            <label>
              <RequiredLabel>
                {form.tipoMeta === 'LEITE'
                  ? 'Preço Médio (R$ / Litro)'
                  : form.tipoMeta === 'ARROBA'
                  ? 'Preço Médio (R$ / Arroba)'
                  : 'Preço Médio (R$)'}
              </RequiredLabel>
              <input
                type="number"
                name="precoMedio"
                value={form.precoMedio}
                onChange={handleChange}
                min="0.01"
                step="0.01"
                placeholder="0,00"
                className={erros.precoMedio ? 'field-error' : ''}
              />
              {erros.precoMedio && (
                <span className="field-error-msg">{erros.precoMedio}</span>
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
              {isSaving
                ? 'Salvando...'
                : isEdit
                ? 'Salvar Alterações'
                : 'Cadastrar Meta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default MetaFormModal
