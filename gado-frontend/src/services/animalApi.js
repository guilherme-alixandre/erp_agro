import { request } from './apiClient'

// Envia o cadastro de animal para a rota atual do controller:
// POST /api/animais/usuarios/{email}
// O e-mail vai na URL porque o backend usa esse valor para localizar o usuario.
function cadastrarAnimal(email, animal) {
  // Codifica o e-mail para ele poder viajar com seguranca na URL.
  const emailCodificado = encodeURIComponent(email)

  // request é para pegar a url do backend que tá no controller,
  // method é pq tamo criando,
  // body pra ficar padrão insomnia
  return request(`/animais/usuarios/${emailCodificado}`, {
    method: 'POST',
    body: JSON.stringify(animal),
  })
}

export { cadastrarAnimal }
