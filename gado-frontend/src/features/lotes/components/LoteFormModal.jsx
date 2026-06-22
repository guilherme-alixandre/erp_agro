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

function SetorCard({
  mode,
  alocacao,
  setor,
  animaisDisponiveis,
  lotesDisponiveis,
  setoresDisponiveis,
  loteAtualId,
  canTransfer,
  onChangeAnimais,
  onTransferirAnimais,
  onRemove,
}) {
  const [animalModalOpen, setAnimalModalOpen] = useState(false)
  const [selectedForTransfer, setSelectedForTransfer] = useState([])
  const [loteDestinoId, setLoteDestinoId] = useState('')
  const [setorDestinoId, setSetorDestinoId] = useState('')
  const [isTransferindo, setIsTransferindo] = useState(false)
  const [transferenciaError, setTransferenciaError] = useState('')

  // Setor existente carregado do lote: usa fluxo de transferência
  // Setor novo adicionado durante edição: usa fluxo de criação (animaisDisponiveis)
  const isEditExisting = mode === 'edit' && alocacao.animaisAtuais !== undefined
  const animaisAtuais = alocacao.animaisAtuais ?? []

  const ocupacao = isEditExisting ? animaisAtuais.length : alocacao.animaisIds.length
  const capacidade = setor.capacidadeMaxima
  const excedido = !isEditExisting && capacidade > 0 && ocupacao > capacidade

  const disabledAnimalIds = animaisDisponiveis
    .filter((a) => BLOCKED_STATUSES.has(a.statusAnimal))
    .map((a) => a.id)

  const triggerText = isEditExisting
    ? animaisAtuais.length > 0
      ? `${animaisAtuais.length} ${animaisAtuais.length === 1 ? 'animal' : 'animais'} neste setor`
      : null
    : buildAnimalTriggerText(alocacao.animaisIds, animaisDisponiveis)

  const mesmoLoteSetor =
    loteDestinoId !== '' &&
    setorDestinoId !== '' &&
    Number(loteDestinoId) === loteAtualId &&
    Number(setorDestinoId) === setor.id

  async function handleConfirmarTransferencia() {
    if (!loteDestinoId || !setorDestinoId || mesmoLoteSetor) return
    setIsTransferindo(true)
    setTransferenciaError('')
    try {
      await onTransferirAnimais(
        selectedForTransfer,
        Number(loteDestinoId),
        Number(setorDestinoId),
      )
      setSelectedForTransfer([])
      setLoteDestinoId('')
      setSetorDestinoId('')
    } catch (error) {
      setTransferenciaError(error.message || 'Falha ao transferir animal(is).')
    } finally {
      setIsTransferindo(false)
    }
  }

  function handleCancelarTransferencia() {
    setSelectedForTransfer([])
    setLoteDestinoId('')
    setSetorDestinoId('')
    setTransferenciaError('')
  }

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
        <span>
          {isEditExisting
            ? canTransfer
              ? 'Selecionar animais para transferir'
              : 'Animais neste setor'
            : 'Animais neste setor'}
        </span>

        {isEditExisting && !canTransfer ? (
          <span className="ssm-trigger ssm-trigger--readonly">
            {triggerText ?? 'Nenhum animal'}
          </span>
        ) : (
          <button
            type="button"
            className="ssm-trigger"
            onClick={() => setAnimalModalOpen(true)}
          >
            <span className={triggerText ? '' : 'ssm-trigger__placeholder'}>
              {isEditExisting
                ? triggerText ?? 'Nenhum animal neste setor'
                : triggerText ?? 'Selecionar animais...'}
            </span>
            <span className="ssm-trigger__arrow" aria-hidden="true">
              ▼
            </span>
          </button>
        )}
      </div>

      {excedido ? (
        <p className="setor-card__aviso">
          Atenção: capacidade máxima ({capacidade}) excedida.
        </p>
      ) : null}

      {isEditExisting && canTransfer && selectedForTransfer.length > 0 ? (
        <div className="setor-card__transferencia">
          <p className="setor-card__transferencia-info">
            {selectedForTransfer.length}{' '}
            {selectedForTransfer.length === 1
              ? 'animal selecionado'
              : 'animais selecionados'}{' '}
            para transferir
          </p>

          <label>
            <span>
              Lote de destino{' '}
              <span className="required-marker" aria-hidden="true">
                *
              </span>
            </span>
            <select
              value={loteDestinoId}
              onChange={(e) => {
                setLoteDestinoId(e.target.value)
                setSetorDestinoId('')
              }}
            >
              <option value="">Selecione um lote...</option>
              {(lotesDisponiveis ?? []).map((l) => (
                <option key={l.id} value={l.id}>
                  {l.codigo}
                  {l.descricao ? ` — ${l.descricao}` : ''}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>
              Setor de destino{' '}
              <span className="required-marker" aria-hidden="true">
                *
              </span>
            </span>
            <select
              value={setorDestinoId}
              onChange={(e) => setSetorDestinoId(e.target.value)}
            >
              <option value="">Selecione um setor...</option>
              {setoresDisponiveis.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nome}
                </option>
              ))}
            </select>
          </label>

          {mesmoLoteSetor ? (
            <p className="feedback feedback--error">
              O animal já está neste lote e setor. Escolha um destino diferente.
            </p>
          ) : null}

          {transferenciaError && !mesmoLoteSetor ? (
            <p className="feedback feedback--error">{transferenciaError}</p>
          ) : null}

          <div className="setor-card__transferencia-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={handleCancelarTransferencia}
              disabled={isTransferindo}
            >
              Cancelar
            </button>
            <button
              type="button"
              className="btn-primary"
              onClick={handleConfirmarTransferencia}
              disabled={
                isTransferindo ||
                !loteDestinoId ||
                !setorDestinoId ||
                mesmoLoteSetor
              }
            >
              {isTransferindo ? 'Transferindo...' : 'Confirmar transferência'}
            </button>
          </div>
        </div>
      ) : null}

      {animalModalOpen ? (
        <SearchSelectModal
          title={
            isEditExisting
              ? 'Selecionar animais para transferir'
              : 'Selecionar animais'
          }
          items={isEditExisting ? animaisAtuais : animaisDisponiveis}
          selectedIds={isEditExisting ? selectedForTransfer : alocacao.animaisIds}
          onConfirm={(ids) => {
            if (isEditExisting) {
              setSelectedForTransfer(ids)
            } else {
              onChangeAnimais(ids)
            }
            setAnimalModalOpen(false)
          }}
          onClose={() => setAnimalModalOpen(false)}
          multiSelect
          columns={ANIMAL_COLUMNS}
          filterField={isEditExisting ? undefined : ANIMAL_FILTER}
          disabledIds={isEditExisting ? [] : disabledAnimalIds}
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
  lotesDisponiveis,
  loteAtualId,
  canTransfer,
  currentUser,
  onClose,
  onChange,
  onChangeAlocacoes,
  onTransferirAnimais,
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
                      mode={mode}
                      alocacao={aloc}
                      setor={setor}
                      animaisDisponiveis={animaisDisponiveis}
                      lotesDisponiveis={lotesDisponiveis}
                      setoresDisponiveis={setoresDisponiveis}
                      loteAtualId={loteAtualId}
                      canTransfer={canTransfer}
                      onChangeAnimais={(ids) => handleAnimaisChange(aloc.setorId, ids)}
                      onTransferirAnimais={onTransferirAnimais}
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
