import { request } from '../../../integration/apiClient'

function toCadastroPayload(usuario) {
  return {
    nome: usuario.nome.trim(),
    email: usuario.email.trim(),
    senha: usuario.senha,
    perfil: usuario.perfil,
  }
}

function isUsuarioDto(payload) {
  return (
    payload &&
    typeof payload === 'object' &&
    !Array.isArray(payload) &&
    ('email' in payload || 'nome' in payload || 'perfil' in payload)
  )
}

function getUsuarioDoPayload(payload) {
  if (!payload) return null
  if (isUsuarioDto(payload)) return payload

  if (payload && typeof payload === 'object' && !Array.isArray(payload)) {
    return payload['Usuário'] ?? payload.usuario ?? payload.mensagem ?? null
  }

  return null
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

async function atualizarUsuario(email, data, adminEmail) {
  const emailCodificado = encodeURIComponent(String(email).trim())
  const payload = await request(`/usuarios/${emailCodificado}`, {
    method: 'PUT',
    headers: adminHeaders(adminEmail),
    body: JSON.stringify(data),
  })
  const usuario = getUsuarioDoPayload(payload)
  return usuario ?? payload
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

async function verificarCredenciais(email, senha) {
  const payload = await request('/usuarios/login', {
    method: 'POST',
    body: JSON.stringify({ email: String(email ?? '').trim(), senha }),
  })
  const usuario = getUsuarioDoPayload(payload)
  if (!usuario) throw new Error('Credenciais inválidas.')
  return usuario
}

export {
  atualizarUsuario,
  buscarUsuarioPorEmail,
  cadastrarUsuario,
  deletarUsuario,
  listarUsuarios,
  loginUsuario,
  verificarCredenciais,
}
