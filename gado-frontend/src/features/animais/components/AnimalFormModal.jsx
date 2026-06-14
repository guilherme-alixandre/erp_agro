import VacinaSelect from './VacinaSelect'

const STATUS_OPTIONS = ['ABATIDO', 'OBITO', 'ATIVO', 'OBSERVACAO', 'VENDIDO']

const MIN_BIRTH_DATE = '1990-01-01'

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
      {children} <span className="required-marker" aria-hidden="true">*</span>
    </span>
  )
}

function AnimalFormModal({
  mode,
  formData,
  isSaving,
  feedback,
  userEmail,
  vacinasDisponiveis,
  lotesDisponiveis,
  loteVinculo,
  setorVinculo,
  onClose,
  onChange,
  onSubmit,
  onAddVacina,
  onChangeVacina,
  onRemoveVacina,
  onChangeLote,
  onChangeSetor,
}) {
  const isCreate = mode === 'create'
  const title = isCreate ? 'Cadastrar animal' : 'Editar animal'
  const submitText = isSaving
    ? 'Salvando...'
    : isCreate
      ? 'Cadastrar'
      : 'Salvar alterações'
  const maxBirthDate = todayIso()

  const loteSelecionado = loteVinculo
    ? (lotesDisponiveis ?? []).find((l) => l.id === loteVinculo) ?? null
    : null

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>{title}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={onSubmit}>

          {isCreate && userEmail ? (
            <p className="form-info">
              Cadastrando como <strong>{userEmail}</strong>
            </p>
          ) : null}

          <label>
            <RequiredLabel>Código do brinco</RequiredLabel>
            <input
              type="text"
              name="codigoBrinco"
              value={formData.codigoBrinco}
              onChange={onChange}
              required
              disabled={!isCreate}
            />
          </label>

          <label>
            <RequiredLabel>Nome</RequiredLabel>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <RequiredLabel>Data de nascimento</RequiredLabel>
            <input
              type="date"
              name="dataNascimento"
              value={formData.dataNascimento}
              onChange={onChange}
              min={MIN_BIRTH_DATE}
              max={maxBirthDate}
              required
            />
          </label>

          <label>
            <RequiredLabel>Peso atual (kg)</RequiredLabel>
            <input
              type="number"
              min="0"
              max="1500"
              step="0.01"
              name="pesoAtual"
              value={formData.pesoAtual}
              onChange={onChange}
              placeholder="Ex.: 320.5"
              required
            />
          </label>

          <label>
            <RequiredLabel>Raça</RequiredLabel>
            <input
              type="text"
              name="raca"
              value={formData.raca}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <RequiredLabel>Cor</RequiredLabel>
            <input
              type="text"
              name="cor"
              value={formData.cor}
              onChange={onChange}
              required
            />
          </label>

          <p className="form-help">
            Medidas zootécnicas — preencha as que você medir.
          </p>

          <label>
            <span>Altura na cernelha (cm)</span>
            <input
              type="number"
              min="0"
              max="350"
              step="0.1"
              name="alturaCernelha"
              value={formData.alturaCernelha}
              onChange={onChange}
              placeholder="cm"
            />
          </label>

          <label>
            <span>Perímetro torácico (cm)</span>
            <input
              type="number"
              min="0"
              max="350"
              step="0.1"
              name="perimetroToracico"
              value={formData.perimetroToracico}
              onChange={onChange}
              placeholder="cm"
            />
          </label>

          <label>
            <span>Comprimento corporal (cm)</span>
            <input
              type="number"
              min="0"
              max="350"
              step="0.1"
              name="comprimentoCorporal"
              value={formData.comprimentoCorporal}
              onChange={onChange}
              placeholder="cm"
            />
          </label>

          <label>
            <span>Sexo</span>
            <select name="sexo" value={formData.sexo} onChange={onChange}>
              <option value="M">Macho</option>
              <option value="F">Fêmea</option>
            </select>
          </label>

          <label>
            <span>Status</span>
            <select
              name="statusAnimal"
              value={formData.statusAnimal}
              onChange={onChange}
            >
              {STATUS_OPTIONS.map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
          </label>

          {isCreate ? (
            <fieldset className="vacinas-fieldset">
              <legend>Vacinas (opcional)</legend>
              <p className="form-help">
                Adicione as vacinas já aplicadas neste animal.
              </p>

              {(formData.vacinas ?? []).length === 0 ? (
                <p className="vacinas-empty">Nenhuma vacina adicionada.</p>
              ) : (
                <ul className="vacinas-list">
                  {formData.vacinas.map((vacina, index) => (
                    <li key={index} className="vacinas-row">
                      <label>
                        <span>Nome</span>
                        <VacinaSelect
                          value={vacina.nome ?? ''}
                          vacinasDisponiveis={vacinasDisponiveis}
                          onChange={(nome) =>
                            onChangeVacina(index, 'nome', nome)
                          }
                          placeholder="Buscar vacina cadastrada"
                        />
                      </label>
                      <label>
                        <span>Data</span>
                        <input
                          type="date"
                          value={vacina.dataOcorrencia ?? ''}
                          onChange={(e) =>
                            onChangeVacina(index, 'dataOcorrencia', e.target.value)
                          }
                          max={maxBirthDate}
                        />
                      </label>
                      <button
                        type="button"
                        className="btn-secondary vacinas-remove"
                        onClick={() => onRemoveVacina(index)}
                        aria-label="Remover vacina"
                      >
                        Remover
                      </button>
                    </li>
                  ))}
                </ul>
              )}

              <button
                type="button"
                className="btn-secondary"
                onClick={onAddVacina}
              >
                + Adicionar vacina
              </button>
            </fieldset>
          ) : null}

          {isCreate ? (
            <fieldset className="vacinas-fieldset">
              <legend>Vincular a um lote (opcional)</legend>

              {(lotesDisponiveis ?? []).length === 0 ? (
                <p className="vacinas-empty">Nenhum lote ativo disponível.</p>
              ) : (
                <>
                  <label>
                    <span>Lote</span>
                    <select value={loteVinculo ?? ''} onChange={onChangeLote}>
                      <option value="">Nenhum</option>
                      {(lotesDisponiveis ?? []).map((l) => (
                        <option key={l.id} value={l.id}>
                          {l.codigo}{l.descricao ? ` — ${l.descricao}` : ''}
                        </option>
                      ))}
                    </select>
                  </label>

                  {loteSelecionado ? (
                    <label>
                      <span>Setor</span>
                      <select value={setorVinculo ?? ''} onChange={onChangeSetor}>
                        <option value="">Selecione um setor</option>
                        {loteSelecionado.alocacoes.map((aloc) => (
                          <option key={aloc.loteSectorId} value={aloc.loteSectorId}>
                            {aloc.setorNome}
                          </option>
                        ))}
                      </select>
                    </label>
                  ) : null}
                </>
              )}
            </fieldset>
          ) : null}

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

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
    </div>
  )
}

export default AnimalFormModal
