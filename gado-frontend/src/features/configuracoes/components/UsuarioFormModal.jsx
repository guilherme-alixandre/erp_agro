const PERFIL_OPTIONS = ['GERENTE', 'CUIDADOR', 'FINANCEIRO', 'ADMINISTRADOR']

function UsuarioFormModal({
  formData,
  isSaving,
  feedback,
  onClose,
  onChange,
  onSubmit,
}) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-card">
        <div className="modal-header">
          <h2>Cadastrar usuário</h2>
          <button type="button" className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>

        <form className="animal-form" onSubmit={onSubmit}>
          <p className="form-info">
            O usuário poderá entrar usando o e-mail e a senha definidos aqui.
            Compartilhe os dados com ele de forma segura.
          </p>

          <label>
            <span>
              Nome <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="text"
              name="nome"
              value={formData.nome}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>
              E-mail <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={onChange}
              required
            />
          </label>

          <label>
            <span>
              Senha inicial <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <input
              type="password"
              name="senha"
              value={formData.senha}
              onChange={onChange}
              minLength={4}
              required
            />
          </label>

          <label>
            <span>
              Perfil <span className="required-marker" aria-hidden="true">*</span>
            </span>
            <select name="perfil" value={formData.perfil} onChange={onChange}>
              {PERFIL_OPTIONS.map((perfil) => (
                <option key={perfil} value={perfil}>
                  {perfil}
                </option>
              ))}
            </select>
          </label>

          {feedback ? <p className="feedback feedback--error">{feedback}</p> : null}

          <div className="modal-actions">
            <button type="button" className="btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="btn-primary" disabled={isSaving}>
              {isSaving ? 'Cadastrando...' : 'Cadastrar usuário'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default UsuarioFormModal
