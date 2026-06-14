import { request } from './apiClient'

function toCadastroPayload(usuario) {
  return {
    nome: String(usuario?.nome ?? '').trim(),
    email: String(usuario?.email ?? '').trim(),
    senha: usuario?.senha ?? '',
    perfil: usuario?.perfil,
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
  const emailCodificado = encodeURIComponent(String(email ?? '').trim())
  const payload = await request(`/usuarios/${emailCodificado}`)
  const usuario = getUsuarioDoPayload(payload)

  if (!usuario || typeof usuario !== 'object' || Array.isArray(usuario)) {
    throw new Error(payload?.Erro ?? payload?.erro ?? 'Usuário não encontrado.')
  }

  return usuario
}

async function cadastrarUsuario(usuario, adminEmail) {
  const payload = await request('/usuarios', {
    method: 'POST',
    headers: {
      ...adminHeaders(adminEmail),       // <-- Espalha os cabeçalhos de autenticação
      'Content-Type': 'application/json' // <-- Adiciona o aviso de que o body é JSON
    },
    body: JSON.stringify(toCadastroPayload(usuario)),
  })

  // Processa a resposta normalmente
  const created = getUsuarioDoPayload(payload)
  return created ?? payload
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
  const trimmedEmail = String(email ?? '').trim()

  // Se existir endpoint de login no futuro, tentamos primeiro.
  try {
    const payload = await request('/usuarios/login', {
      method: 'POST',
      body: JSON.stringify({
        email: trimmedEmail,
        senha,
      }),
    })

    const usuario = getUsuarioDoPayload(payload)
    if (usuario && typeof usuario === 'object' && !Array.isArray(usuario)) {
      return usuario
    }
  } catch (error) {
    // fallback para o fluxo atual: "login" = buscar por e-mail
    const message = String(error?.message ?? '')
    if (!message.includes('Recurso') && !message.includes('404')) {
      throw error
    }
  }

  // fallback: busca pelo e-mail (senha não é validada no backend neste momento)
  return buscarUsuarioPorEmail(trimmedEmail)
}

export {
  buscarUsuarioPorEmail,
  cadastrarUsuario,
  deletarUsuario,
  listarUsuarios,
  loginUsuario,
}

