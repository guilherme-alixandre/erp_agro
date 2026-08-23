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

async function listarUsuarios() {
  const payload = await request('/usuarios')
  if (!Array.isArray(payload)) {
    throw new Error('Resposta inesperada ao listar usuários.')
  }
  return payload
}

async function atualizarUsuario(email, data) {
  const emailCodificado = encodeURIComponent(String(email).trim())
  const payload = await request(`/usuarios/${emailCodificado}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  })
  const usuario = getUsuarioDoPayload(payload)
  return usuario ?? payload
}

function deletarUsuario(email) {
  const emailCodificado = encodeURIComponent(String(email).trim())
  return request(`/usuarios/${emailCodificado}`, { method: 'DELETE' })
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

  return { usuario, token: payload?.token ?? null }
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
