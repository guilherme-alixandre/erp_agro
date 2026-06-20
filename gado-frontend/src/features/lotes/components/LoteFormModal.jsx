import { useState } from 'react'
import SearchSelectModal from '../../../components/shared/SearchSelectModal'

const ANIMAL_COLUMNS = [
  { key: 'codigoBrinco', label: 'Código' },
  { key: 'nome', label: 'Nome' },
]

const ANIMAL_FILTER = {
  label: 'Status',
  key: 'statusAnimal',
  options: [
    { value: 'ATIVO', label: 'Ativo' },
    { value: 'OBSERVACAO', label: 'Observação' },
    { value: 'VENDIDO', label: 'Vendido' },
    { value: 'OBITO', label: 'Óbito' },
    { value: 'ABATIDO', label: 'Abatido' },
  ],
}

const SETOR_COLUMNS = [{ key: 'nome', label: 'Nome' }]

const BLOCKED_STATUSES = new Set(['VENDIDO', 'OBITO', 'ABATIDO'])

function todayIso() {
  const now = new Date()
  const yyyy = now.getFullYear()
  const mm = String(now.getMonth() + 1).padStart(2, '0')
  const dd = String(now.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

function RequiredLabel({ children }) {
  return (
    <span>
      {children}{' '}
      <span className="required-marker" aria-hidden="true">
        *
      </span>
    </span>
  )
}

function buildAnimalTriggerText(selectedIds, animaisDisponiveis) {
  if (selectedIds.length === 0) return null
  if (selectedIds.length === 1) {
    const found = animaisDisponiveis.find((a) => a.id === selectedIds[0])
    if (found) {
      return found.nome ? `${found.codigoBrinco} — ${found.nome}` : found.codigoBrinco
    }
    return '1 selecionado'
  }
  return `${selectedIds.length} animais selecionados`
}

function buildSetorTriggerText(selectedIds, setoresDisponiveis) {
  if (selectedIds.length === 0) return null
  if (selectedIds.length === 1) {
    const found = setoresDisponiveis.find((s) => s.id === selectedIds[0])
    return found ? found.nome : '1 setor selecionado'
  }
  return `${selectedIds.length} setores selecionados`
}

function SetorCard({ alocacao, setor, animaisDisponiveis, onChangeAnimais, onRemove }) {
  const [animalModalOpen, setAnimalModalOpen] = useState(false)

  const disabledAnimalIds = animaisDisponiveis
    .filter((a) => BLOCKED_STATUSES.has(a.statusAnimal))
    .map((a) => a.id)

  const ocupacao = alocacao.animaisIds.length
  const capacidade = setor.capacidadeMaxima
  const excedido = capacidade > 0 && ocupacao > capacidade

  const triggerText = buildAnimalTriggerText(alocacao.animaisIds, animaisDisponiveis)

  return (
    <div className="setor-card">
      <div className="setor-card__header">
        <strong className="setor-card__nome">{setor.nome}</strong>
        <span
          className={`setor-card__capacidade${excedido ? ' setor-card__capacidade--excedido' : ''}`}
        >
          {ocupacao}/{capacidade} animais
        </span>
        <button
          type="button"
          className="setor-card__remover"
          onClick={onRemove}
          aria-label={`Remover setor ${setor.nome}`}
        >
          ✕
        </button>
      </div>

      <div className="setor-card__label">
        <span>Animais neste setor</span>
        <button
          type="button"
          className="ssm-trigger"
          onClick={() => setAnimalModalOpen(true)}
        >
          <span className={triggerText ? '' : 'ssm-trigger__placeholder'}>
            {triggerText ?? 'Selecionar animais...'}
          </span>
          <span className="ssm-trigger__arrow" aria-hidden="true">
            ▼
          </span>
        </button>
      </div>

      {excedido ? (
        <p className="setor-card__aviso">
          Atenção: capacidade máxima ({capacidade}) excedida.
        </p>
      ) : null}

      {animalModalOpen ? (
        <SearchSelectModal
          title="Selecionar animais"
          items={animaisDisponiveis}
          selectedIds={alocacao.animaisIds}
          onConfirm={(ids) => {
            onChangeAnimais(ids)
            setAnimalModalOpen(false)
          }}
          onClose={() => setAnimalModalOpen(false)}
          multiSelect
          columns={ANIMAL_COLUMNS}
          filterField={ANIMAL_FILTER}
          disabledIds={disabledAnimalIds}
        />
      ) : null}
    </div>
  )
}

function LoteFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  setoresDisponiveis,
  animaisDisponiveis,
  currentUser,
  onClose,
  onChange,
  onChangeAlocacoes,
  onSubmit,
}) {
  const [setorModalOpen, setSetorModalOpen] = useState(false)

  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar lote' : 'Editar lote'
  const submitText = isSaving
    ? 'Salvando...'
    : isCreate
      ? 'Cadastrar'
      : 'Salvar alterações'
  const today = todayIso()

  const selectedSetorIds = formData.alocacoes.map((a) => a.setorId)

  function handleSetorSelectionChange(newSetorIds) {
    const newAlocacoes = newSetorIds.map((setorId) => {
      const existing = formData.alocacoes.find((a) => a.setorId === setorId)
      return existing ?? { setorId, animaisIds: [] }
    })
    onChangeAlocacoes(newAlocacoes)
    setSetorModalOpen(false)
  }

  function handleAnimaisChange(setorId, newAnimaisIds) {
    const newAlocacoes = formData.alocacoes.map((a) =>
      a.setorId === setorId ? { ...a, animaisIds: newAnimaisIds } : a,
    )
    onChangeAlocacoes(newAlocacoes)
  }

  function handleRemoveSetor(setorId) {
    onChangeAlocacoes(formData.alocacoes.filter((a) => a.setorId !== setorId))
  }

  const setorTriggerText = buildSetorTriggerText(selectedSetorIds, setoresDisponiveis)

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="lote-form" onSubmit={onSubmit}>
          {isCreate ? (
            <p className="form-info">
              Cadastrando como <strong>{currentUser.email}</strong>
            </p>
          ) : (
            <label>
              <span>Código do lote</span>
              <input type="text" value={formData.codigo ?? ''} readOnly disabled />
            </label>
          )}

          <label>
            <RequiredLabel>Cor do brinco</RequiredLabel>
            <input
              type="text"
              name="corBrinco"
              value={formData.corBrinco}
              onChange={onChange}
              placeholder="Ex.: Amarelo, Vermelho"
              required
            />
          </label>

          <label>
            <span>Descrição</span>
            <input
              type="text"
              name="descricao"
              value={formData.descricao}
              onChange={onChange}
              placeholder="Descrição opcional do lote"
            />
          </label>

          <label>
            <span>Raça predominante</span>
            <input
              type="text"
              name="racaPredominante"
              value={formData.racaPredominante}
              onChange={onChange}
              placeholder="Ex.: Nelore, Angus"
            />
          </label>

          <label>
            <span>Data de criação</span>
            <input
              type="date"
              name="dataCriacao"
              value={formData.dataCriacao}
              onChange={onChange}
              max={today}
            />
          </label>

          <fieldset className="setores-fieldset">
            <legend>
              <RequiredLabel>Setores e alocação de animais</RequiredLabel>
            </legend>
            <p className="form-help">
              Selecione os setores que este lote vai ocupar. Para cada setor,
              escolha os animais alocados.
            </p>

            <div className="setores-fieldset__select">
              <span>Selecionar setores</span>
              <button
                type="button"
                className="ssm-trigger"
                onClick={() => setSetorModalOpen(true)}
              >
                <span className={setorTriggerText ? '' : 'ssm-trigger__placeholder'}>
                  {setorTriggerText ?? 'Escolha um ou mais setores...'}
                </span>
                <span className="ssm-trigger__arrow" aria-hidden="true">
                  ▼
                </span>
              </button>
            </div>

            {formData.alocacoes.length > 0 ? (
              <div className="setores-cards">
                {formData.alocacoes.map((aloc) => {
                  const setor = setoresDisponiveis.find((s) => s.id === aloc.setorId)
                  if (!setor) return null
                  return (
                    <SetorCard
                      key={aloc.setorId}
                      alocacao={aloc}
                      setor={setor}
                      animaisDisponiveis={animaisDisponiveis}
                      onChangeAnimais={(ids) => handleAnimaisChange(aloc.setorId, ids)}
                      onRemove={() => handleRemoveSetor(aloc.setorId)}
                    />
                  )
                })}
              </div>
            ) : null}
          </fieldset>

          {feedback ? (
            <p className="feedback feedback--error">{feedback}</p>
          ) : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {submitText}
            </button>
          </div>
        </form>
      </div>

      {setorModalOpen ? (
        <SearchSelectModal
          title="Selecionar setores"
          items={setoresDisponiveis}
          selectedIds={selectedSetorIds}
          onConfirm={handleSetorSelectionChange}
          onClose={() => setSetorModalOpen(false)}
          multiSelect
          columns={SETOR_COLUMNS}
        />
      ) : null}
    </div>
  )
}

export default LoteFormModal
