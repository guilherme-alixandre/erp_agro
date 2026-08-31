import { useEffect, useMemo, useRef, useState } from 'react'

const SIDEBAR_WIDTH_KEY = 'erp_agro_sidebar_width'
const SIDEBAR_MIN_WIDTH = 238
const SIDEBAR_MAX_WIDTH = 420

function loadSidebarWidth() {
  const savedWidth = Number(localStorage.getItem(SIDEBAR_WIDTH_KEY))
  if (!Number.isFinite(savedWidth)) return 274
  return Math.min(SIDEBAR_MAX_WIDTH, Math.max(SIDEBAR_MIN_WIDTH, savedWidth))
}

const PAGE_META = {
  resumo: { label: 'Resumo', hint: 'Visão geral da fazenda', icon: 'home' },
  animais: { label: 'Animais', hint: 'Rebanho e raças', icon: 'cow' },
  lotes: { label: 'Lotes', hint: 'Grupos e alocações', icon: 'layers' },
  setores: { label: 'Setores', hint: 'Áreas da propriedade', icon: 'map' },
  metas: { label: 'Metas', hint: 'Objetivos e medições', icon: 'target' },
  insumos: { label: 'Insumos', hint: 'Estoque e aplicações', icon: 'box' },
  financeiro: { label: 'Financeiro', hint: 'Entradas, saídas e documentos', icon: 'wallet' },
  tarefas: { label: 'Tarefas', hint: 'Rotina da equipe', icon: 'check' },
  perfil: { label: 'Meu perfil', hint: 'Dados e segurança', icon: 'user' },
  configuracoes: { label: 'Equipe', hint: 'Usuários e permissões', icon: 'settings' },
}

export function AppIcon({ name, size = 20 }) {
  const common = {
    width: size,
    height: size,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.9,
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
    'aria-hidden': true,
  }

  const paths = {
    home: <><path d="M3 10.8 12 3l9 7.8"/><path d="M5.5 9.5V21h13V9.5M9 21v-7h6v7"/></>,
    cow: <><path d="M5 8.5 2.5 5M19 8.5 21.5 5M6 7c1.4-2 3.4-3 6-3s4.6 1 6 3v8.5c0 3-2.7 5.5-6 5.5s-6-2.5-6-5.5Z"/><path d="M8.5 13h.01M15.5 13h.01M9 17c1.8 1.1 4.2 1.1 6 0"/></>,
    layers: <><path d="m12 3-9 5 9 5 9-5-9-5Z"/><path d="m3 12 9 5 9-5M3 16l9 5 9-5"/></>,
    map: <><path d="m3 6 6-3 6 3 6-3v15l-6 3-6-3-6 3V6Z"/><path d="M9 3v15M15 6v15"/></>,
    target: <><circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="5"/><circle cx="12" cy="12" r="1"/></>,
    box: <><path d="m4 7 8-4 8 4-8 4-8-4Z"/><path d="M4 7v10l8 4 8-4V7M12 11v10"/></>,
    wallet: <><path d="M4 5h14a2 2 0 0 1 2 2v12H4a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2Z"/><path d="M16 11h6v5h-6a2.5 2.5 0 0 1 0-5Z"/></>,
    check: <><rect x="3" y="3" width="18" height="18" rx="3"/><path d="m7 12 3 3 7-7"/></>,
    user: <><circle cx="12" cy="8" r="4"/><path d="M4.5 21a7.5 7.5 0 0 1 15 0"/></>,
    settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .34 1.88l.06.06-2.83 2.83-.06-.06a1.7 1.7 0 0 0-1.88-.34 1.7 1.7 0 0 0-1.03 1.56V21h-4v-.08A1.7 1.7 0 0 0 9 19.37a1.7 1.7 0 0 0-1.88.34l-.06.06-2.83-2.83.06-.06A1.7 1.7 0 0 0 4.63 15 1.7 1.7 0 0 0 3.08 14H3v-4h.08A1.7 1.7 0 0 0 4.63 9a1.7 1.7 0 0 0-.34-1.88l-.06-.06 2.83-2.83.06.06A1.7 1.7 0 0 0 9 4.63 1.7 1.7 0 0 0 10 3.08V3h4v.08A1.7 1.7 0 0 0 15 4.63a1.7 1.7 0 0 0 1.88-.34l.06-.06 2.83 2.83-.06.06A1.7 1.7 0 0 0 19.37 9 1.7 1.7 0 0 0 20.92 10H21v4h-.08A1.7 1.7 0 0 0 19.4 15Z"/></>,
    menu: <><path d="M4 6h16M4 12h16M4 18h16"/></>,
    close: <><path d="m6 6 12 12M18 6 6 18"/></>,
    logout: <><path d="M10 17l5-5-5-5M15 12H3"/><path d="M14 3h5a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-5"/></>,
  }

  return <svg {...common}>{paths[name]}</svg>
}

function AppShell({ activePage, currentUser, onNavigate, onLogout, children }) {
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [sidebarWidth, setSidebarWidth] = useState(loadSidebarWidth)
  const [isResizingSidebar, setIsResizingSidebar] = useState(false)
  const resizeStart = useRef({ pointerX: 0, width: 274 })

  const navItems = useMemo(() => {
    const items = ['resumo', 'animais', 'lotes', 'setores', 'metas', 'insumos', 'tarefas']
    if (!['CUIDADOR', 'CUIDADOR_CHEFE'].includes(currentUser?.perfil)) items.push('financeiro')
    items.push('perfil')
    if (currentUser?.perfil === 'ADMINISTRADOR') items.push('configuracoes')
    return items
  }, [currentUser?.perfil])

  useEffect(() => setDrawerOpen(false), [activePage])

  useEffect(() => {
    if (!drawerOpen) return undefined
    const closeOnEscape = (event) => {
      if (event.key === 'Escape') setDrawerOpen(false)
    }
    document.addEventListener('keydown', closeOnEscape)
    return () => document.removeEventListener('keydown', closeOnEscape)
  }, [drawerOpen])

  useEffect(() => {
    if (!isResizingSidebar) return undefined

    function handlePointerMove(event) {
      const movement = event.clientX - resizeStart.current.pointerX
      const nextWidth = Math.min(
        SIDEBAR_MAX_WIDTH,
        Math.max(SIDEBAR_MIN_WIDTH, resizeStart.current.width + movement),
      )
      setSidebarWidth(nextWidth)
    }

    function handlePointerUp() {
      setIsResizingSidebar(false)
    }

    document.body.classList.add('app-is-resizing-sidebar')
    window.addEventListener('pointermove', handlePointerMove)
    window.addEventListener('pointerup', handlePointerUp, { once: true })

    return () => {
      document.body.classList.remove('app-is-resizing-sidebar')
      window.removeEventListener('pointermove', handlePointerMove)
      window.removeEventListener('pointerup', handlePointerUp)
    }
  }, [isResizingSidebar])

  useEffect(() => {
    localStorage.setItem(SIDEBAR_WIDTH_KEY, String(Math.round(sidebarWidth)))
  }, [sidebarWidth])

  function startSidebarResize(event) {
    if (event.button !== 0) return
    event.preventDefault()
    resizeStart.current = { pointerX: event.clientX, width: sidebarWidth }
    setIsResizingSidebar(true)
  }

  function resizeSidebarWithKeyboard(event) {
    let nextWidth = sidebarWidth
    if (event.key === 'ArrowLeft') nextWidth -= 16
    else if (event.key === 'ArrowRight') nextWidth += 16
    else if (event.key === 'Home') nextWidth = SIDEBAR_MIN_WIDTH
    else if (event.key === 'End') nextWidth = SIDEBAR_MAX_WIDTH
    else return

    event.preventDefault()
    setSidebarWidth(Math.min(SIDEBAR_MAX_WIDTH, Math.max(SIDEBAR_MIN_WIDTH, nextWidth)))
  }

  const initials = (currentUser?.nome || currentUser?.email || 'U')
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase()

  function navigate(page) {
    onNavigate(page)
    setDrawerOpen(false)
  }

  function NavButton({ page, compact = false }) {
    const meta = PAGE_META[page]
    const active = activePage === page
    return (
      <button
        type="button"
        className={`app-nav__item${active ? ' app-nav__item--active' : ''}${compact ? ' app-nav__item--compact' : ''}`}
        onClick={() => navigate(page)}
        aria-current={active ? 'page' : undefined}
      >
        <span className="app-nav__icon"><AppIcon name={meta.icon} size={compact ? 22 : 24} /></span>
        <span className="app-nav__copy">
          <strong>{meta.label}</strong>
          {!compact ? <small>{meta.hint}</small> : null}
        </span>
      </button>
    )
  }

  const primaryMobile = ['resumo', 'animais', 'tarefas', 'insumos']

  return (
    <div
      className={`app-shell${isResizingSidebar ? ' app-shell--resizing' : ''}`}
      style={{ '--app-sidebar-width': `${sidebarWidth}px` }}
    >
      <aside className="app-sidebar" aria-label="Navegação principal">
        <button type="button" className="app-brand" onClick={() => navigate('resumo')}>
          <img src="/logo.png" alt="" />
          <span><strong>GADO</strong><small>Gestão rural</small></span>
        </button>

        <div className="app-sidebar__label">Sua fazenda</div>
        <nav className="app-nav">
          {navItems.map((page) => <NavButton key={page} page={page} />)}
        </nav>

        <div className="app-user">
          <button type="button" className="app-user__identity" onClick={() => navigate('perfil')}>
            <span className="app-user__avatar">{initials}</span>
            <span className="app-user__copy"><strong>{currentUser.nome}</strong><small>{currentUser.perfil?.replaceAll('_', ' ')}</small></span>
          </button>
          <button type="button" className="app-user__logout" onClick={onLogout} aria-label="Sair da conta" title="Sair">
            <AppIcon name="logout" size={19} />
          </button>
        </div>

        <div
          className="app-sidebar__resize-handle"
          role="separator"
          aria-label="Redimensionar barra lateral"
          aria-orientation="vertical"
          aria-valuemin={SIDEBAR_MIN_WIDTH}
          aria-valuemax={SIDEBAR_MAX_WIDTH}
          aria-valuenow={Math.round(sidebarWidth)}
          tabIndex="0"
          onPointerDown={startSidebarResize}
          onKeyDown={resizeSidebarWithKeyboard}
        />
      </aside>

      <div className="app-main">
        <header className="app-mobile-header">
          <button type="button" className="app-mobile-brand" onClick={() => navigate('resumo')} aria-label="Ir para o resumo">
            <img src="/logo.png" alt="" />
          </button>
          <div className="app-mobile-title">
            <strong>{PAGE_META[activePage]?.label ?? 'GADO'}</strong>
            <span>{PAGE_META[activePage]?.hint ?? 'Gestão rural'}</span>
          </div>
          <button type="button" className="app-menu-button" onClick={() => setDrawerOpen(true)} aria-label="Abrir menu">
            <AppIcon name="menu" size={24} />
          </button>
        </header>

        <div className="app-page">{children}</div>

        <nav className="app-bottom-nav" aria-label="Atalhos principais">
          {primaryMobile.map((page) => <NavButton key={page} page={page} compact />)}
          <button
            type="button"
            className={`app-nav__item app-nav__item--compact${drawerOpen ? ' app-nav__item--active' : ''}`}
            onClick={() => setDrawerOpen(true)}
          >
            <span className="app-nav__icon"><AppIcon name="menu" size={21} /></span>
            <span className="app-nav__copy"><strong>Mais</strong></span>
          </button>
        </nav>
      </div>

      <div className={`app-drawer${drawerOpen ? ' app-drawer--open' : ''}`} aria-hidden={!drawerOpen}>
        <button type="button" className="app-drawer__backdrop" onClick={() => setDrawerOpen(false)} aria-label="Fechar menu" />
        <aside className="app-drawer__panel" aria-label="Menu completo">
          <div className="app-drawer__header">
            <div className="app-user__identity">
              <span className="app-user__avatar">{initials}</span>
              <span className="app-user__copy"><strong>{currentUser.nome}</strong><small>{currentUser.email}</small></span>
            </div>
            <button type="button" className="app-menu-button" onClick={() => setDrawerOpen(false)} aria-label="Fechar menu">
              <AppIcon name="close" size={24} />
            </button>
          </div>
          <div className="app-drawer__label">Todos os módulos</div>
          <nav className="app-nav app-drawer__nav">
            {navItems.map((page) => <NavButton key={page} page={page} />)}
          </nav>
          <button type="button" className="app-drawer__logout" onClick={onLogout}>
            <AppIcon name="logout" size={19} /> Sair da conta
          </button>
        </aside>
      </div>
    </div>
  )
}

export default AppShell
