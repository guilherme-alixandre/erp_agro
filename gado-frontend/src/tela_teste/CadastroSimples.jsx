import { useState } from 'react'
import { cadastrarAnimal } from '../services/animalApi'

// Estado inicial do formulario.
// Os nomes das chaves seguem os nomes esperados pelo AnimalDto no backend.
const initialForm = {
  emailUsuario: '',
  codigoBrinco: '',
  nome: '',
  dataNascimento: '',
  pesoAtual: '',
  raca: '',
  cor: '',
  tamanho: '',
  sexo: 'M',
  statusAnimal: 'EX1',
}

function CadastroSimples() {
  // Guarda os valores digitados pelo usuario.
  const [formData, setFormData] = useState(initialForm)

  // Mostra mensagens de sucesso ou erro logo abaixo dos campos.
  const [feedback, setFeedback] = useState({ type: '', message: '' })

  // Controla o estado do botao enquanto o envio esta acontecendo.
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Atualiza apenas o campo alterado sem perder os demais valores do formulario.
  function handleChange(event) {
    const { name, value } = event.target
    setFormData((current) => ({
      ...current,
      [name]: value,
    }))
  }

  // Faz o envio do formulario para o backend.
  // Aqui separamos o e-mail, porque ele vai na URL da rota,
  // enquanto o restante dos dados vai no corpo da requisicao.
  async function handleSubmit(event) {
    event.preventDefault()
    setIsSubmitting(true)
    setFeedback({ type: '', message: '' })

    try {
      const { emailUsuario, ...animalPayload } = formData

      // O backend espera peso como numero e data no formato LocalDateTime.
      await cadastrarAnimal(emailUsuario, {
        ...animalPayload,
        pesoAtual: Number(animalPayload.pesoAtual),
        dataNascimento: `${animalPayload.dataNascimento}T00:00:00`,
      })

      // Se der certo, mostramos feedback e limpamos o formulario.
      setFeedback({
        type: 'success',
        message: 'Animal cadastrado com sucesso.',
      })
      setFormData(initialForm)
    } catch (error) {
      // Se der erro, mostramos a mensagem devolvida pelo backend
      // ou uma mensagem padrao caso ela nao exista.
      setFeedback({
        type: 'error',
        message: error.message || 'Nao foi possivel cadastrar o animal.',
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="cadastro-page">
      <section className="cadastro-card">
        <div className="cadastro-header">
          <span className="cadastro-badge">Tela de teste</span>
          <h1>Cadastro de animal</h1>
          <p>Preencha os dados do animal para enviar o cadastro ao backend.</p>
        </div>

        <form className="cadastro-form" onSubmit={handleSubmit}>
          <label className="cadastro-field">
            <span>E-mail do usuario</span>
            <input
              name="emailUsuario"
              type="email"
              placeholder="usuario@exemplo.com"
              value={formData.emailUsuario}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Codigo do brinco</span>
            <input
              name="codigoBrinco"
              type="text"
              placeholder="Ex: BR-001"
              value={formData.codigoBrinco}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Nome</span>
            <input
              name="nome"
              type="text"
              placeholder="Nome do animal"
              value={formData.nome}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Data de nascimento</span>
            <input
              name="dataNascimento"
              type="date"
              value={formData.dataNascimento}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Peso atual</span>
            <input
              name="pesoAtual"
              type="number"
              min="0"
              step="0.01"
              placeholder="Ex: 350.5"
              value={formData.pesoAtual}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Raca</span>
            <input
              name="raca"
              type="text"
              placeholder="Ex: Nelore"
              value={formData.raca}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Cor</span>
            <input
              name="cor"
              type="text"
              placeholder="Ex: Branca"
              value={formData.cor}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Tamanho</span>
            <input
              name="tamanho"
              type="text"
              placeholder="Ex: Medio"
              value={formData.tamanho}
              onChange={handleChange}
              required
            />
          </label>

          <label className="cadastro-field">
            <span>Sexo</span>
            <select name="sexo" value={formData.sexo} onChange={handleChange}>
              <option value="M">Macho</option>
              <option value="F">Femea</option>
            </select>
          </label>

          <label className="cadastro-field">
            <span>Status do animal</span>
            <select
              name="statusAnimal"
              value={formData.statusAnimal}
              onChange={handleChange}
            >
              <option value="EX1">EX1</option>
              <option value="EX2">EX2</option>
            </select>
          </label>

          {feedback.message ? (
            <p className={`cadastro-feedback cadastro-feedback--${feedback.type}`}>
              {feedback.message}
            </p>
          ) : null}

          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando...' : 'Cadastrar animal'}
          </button>
        </form>
      </section>
    </main>
  )
}

export default CadastroSimples
