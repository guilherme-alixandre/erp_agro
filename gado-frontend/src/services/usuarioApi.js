import { request } from './apiClient'

function toCadastroPayload(usuario) {
  return {
    nome: usuario.nome.trim(),
    email: usuario.email.trim(),
    senha: usuario.senha,
    perfil: usuario.perfil,
  }
}

function getUsuarioDoPayload(payload) {
  if (!payload || typeof payload !== 'object') return null
  return payload['Usuário'] ?? payload.usuario ?? payload.mensagem ?? null
}

async function buscarUsuarioPorEmail(email) {
  const emailCodificado = encodeURIComponent(email.trim())
  const payload = await request(`/usuarios/${emailCodificado}`)
  const usuario = getUsuarioDoPayload(payload)

  if (!usuario || typeof usuario !== 'object' || Array.isArray(usuario)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Usuário não encontrado.')
  }

  return usuario
}

function cadastrarUsuario(usuario) {
  return request('/usuarios', {
    method: 'POST',
    body: JSON.stringify(toCadastroPayload(usuario)),
  })
}

async function loginUsuario(email, senha) {
  const payload = await request('/usuarios/login', {
    method: 'POST',
    body: JSON.stringify({
      email: email.trim(),
      senha,
    }),
  })

  const usuario = getUsuarioDoPayload(payload)
  if (!usuario || typeof usuario !== 'object' || Array.isArray(usuario)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Credenciais inválidas.')
  }

  return usuario
}

export { buscarUsuarioPorEmail, cadastrarUsuario, loginUsuario }
