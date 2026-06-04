import { request } from '../../../integration/apiClient'

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

function adminHeaders(adminEmail) {
  if (!adminEmail) return {}
  return { 'X-Admin-Email': String(adminEmail).trim() }
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

function cadastrarUsuario(usuario, adminEmail) {
  return request('/usuarios', {
    method: 'POST',
    headers: adminHeaders(adminEmail),
    body: JSON.stringify(toCadastroPayload(usuario)),
  })
}

async function listarUsuarios(adminEmail) {
  const payload = await request('/usuarios', {
    headers: adminHeaders(adminEmail),
  })
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar usuários.')
  }
  return payload
}

function deletarUsuario(email, adminEmail) {
  const emailCodificado = encodeURIComponent(String(email).trim())
  return request(`/usuarios/${emailCodificado}`, {
    method: 'DELETE',
    headers: adminHeaders(adminEmail),
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

export {
  buscarUsuarioPorEmail,
  cadastrarUsuario,
  deletarUsuario,
  listarUsuarios,
  loginUsuario,
}
