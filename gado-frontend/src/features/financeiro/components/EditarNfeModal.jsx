import { useState } from 'react'

function toDateInputValue(iso) {
  if (!iso) return ''
  return String(iso).slice(0, 10)
}

/**
 * Dupla validação de segurança: os campos de edição só aparecem depois que a senha do
 * Admin/Gerente logado é informada e confirmada com "Desbloquear edição". A senha em si só é
 * validada de fato no backend (SDocumentoEntrada.editarNfe, contra o hash do usuário) — não há
 * endpoint de "checar senha" isolado, então este passo é o gate de UX, e a mesma senha viaja
 * junto com a correção no submit final. O campo de senha continua editável depois do
 * desbloqueio (não some) justamente para que, se o backend recusar a senha, o usuário possa
 * corrigi-la e reenviar sem perder as edições já feitas no formulário.
 */
function EditarNfeModal({ documento, isSaving, feedback, onClose, onSubmit }) {
  const [senha, setSenha] = useState('')
  const [unlocked, setUnlocked] = useState(false)
  const [erroSenha, setErroSenha] = useState('')

  const [form, setForm] = useState({
    numeroDocumento: documento.numeroDocumento ?? '',
    serie: documento.serie ?? '',
    dataEmissao: toDateInputValue(documento.dataEmissao),
    valorTotal: documento.valorTotal ?? '',
    itens: documento.itens.map((item) => ({
      itemId: item.id,
      descricaoXml: item.descricaoXml,
      quantidade: item.quantidade,
      valorUnitario: item.valorUnitario,
      valorTotal: item.valorTotal,
    })),
  })

  function handleDesbloquear(event) {
    event.preventDefault()
    if (!senha.trim()) {
      setErroSenha('Informe sua senha para liberar a edição.')
      return
    }
    setErroSenha('')
    setUnlocked(true)
  }

  function handleFieldChange(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  function handleItemChange(itemId, campo, value) {
    setForm((current) => ({
      ...current,
      itens: current.itens.map((item) => (item.itemId === itemId ? { ...item, [campo]: value } : item)),
    }))
  }

  function handleSubmit(event) {
    event.preventDefault()
    onSubmit({ ...form, senhaConfirmacao: senha })
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card modal-card--wide">
        <div className="modal-header">
          <h2>Editar NF-e {documento.numeroDocumento ? `#${documento.numeroDocumento}` : ''}</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        {!unlocked ? (
          <form className="animal-form financeiro-senha-gate" onSubmit={handleDesbloquear}>
            <p className="form-info financeiro-senha-gate__aviso">
              🔒 Corrigir dados de uma NF-e importada exige confirmar sua senha de Administrador
              ou Gerente. Os campos só aparecem depois disso.
            </p>

            <label>
              <span>
                Sua senha <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                autoFocus
                required
              />
            </label>

            {erroSenha ? <p className="feedback feedback--error">{erroSenha}</p> : null}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={onClose}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary">
                Desbloquear edição
              </button>
            </div>
          </form>
        ) : (
          <form className="animal-form" onSubmit={handleSubmit}>
            <p className="form-info financeiro-senha-gate__ok">✓ Senha informada — edição liberada.</p>

            <label>
              <span>
                Sua senha <span className="required-marker" aria-hidden="true">*</span>
              </span>
              <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
              />
            </label>

            <div className="financeiro-edit-grid">
              <label>
                <span>Número do documento</span>
                <input type="text" name="numeroDocumento" value={form.numeroDocumento} onChange={handleFieldChange} />
              </label>
              <label>
                <span>Série</span>
                <input type="text" name="serie" value={form.serie} onChange={handleFieldChange} />
              </label>
              <label>
                <span>Data de emissão</span>
                <input type="date" name="dataEmissao" value={form.dataEmissao} onChange={handleFieldChange} />
              </label>
              <label>
                <span>Valor total (R$)</span>
                <input
                  type="number"
                  name="valorTotal"
                  value={form.valorTotal}
                  onChange={handleFieldChange}
                  min="0"
                  step="0.01"
                />
              </label>
            </div>

            <h3 className="financeiro-edit-itens-title">Itens</h3>
            <div className="data-table-wrapper">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Descrição (do XML — não editável)</th>
                    <th>Quantidade</th>
                    <th>Valor unitário</th>
                    <th>Valor total</th>
                  </tr>
                </thead>
                <tbody>
                  {form.itens.map((item) => (
                    <tr key={item.itemId}>
                      <td>{item.descricaoXml}</td>
                      <td>
                        <input
                          type="number"
                          value={item.quantidade}
                          min="0"
                          step="0.001"
                          onChange={(e) => handleItemChange(item.itemId, 'quantidade', e.target.value)}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          value={item.valorUnitario}
                          min="0"
                          step="0.0001"
                          onChange={(e) => handleItemChange(item.itemId, 'valorUnitario', e.target.value)}
                        />
                      </td>
                      <td>
                        <input
                          type="number"
                          value={item.valorTotal}
                          min="0"
                          step="0.01"
                          onChange={(e) => handleItemChange(item.itemId, 'valorTotal', e.target.value)}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

            <div className="modal-actions">
              <button type="button" className="btn-secondary" onClick={onClose} disabled={isSaving}>
                Cancelar
              </button>
              <button type="submit" className="btn-primary" disabled={isSaving}>
                {isSaving ? 'Salvando...' : 'Salvar correção'}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

export default EditarNfeModal
