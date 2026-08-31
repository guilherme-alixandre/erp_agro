import { useState } from 'react'
import NotasFiscaisTab from '../components/NotasFiscaisTab'
import AprovacoesTab from '../components/AprovacoesTab'
import FuncionariosTab from '../components/FuncionariosTab'
import ParceirosTab from '../components/ParceirosTab'
import DashboardTab from '../components/DashboardTab'
import ModuleHeader from '../../../components/shared/ModuleHeader'
import '../../animais/styles/animais.css'
import '../../insumos/styles/insumos.css'
import '../styles/financeiro.css'

function FinanceiroPage({ currentUser, onNavigate, onLogout }) {
  const [activeTab, setActiveTab] = useState('nfs')

  return (
    <main className="animals-layout">
      <aside className="animals-sidebar">
        <div className="animals-logo"><img src="/logo.png" alt="GADO" /></div>
        <nav>
          <button type="button" className="menu-item" onClick={() => onNavigate('resumo')}>
            Resumo
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('animais')}>
            Animais
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('lotes')}>
            Lotes
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('setores')}>
            Setores
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('metas')}>
            Metas
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('insumos')}>
            Insumos
          </button>
          <button type="button" className="menu-item menu-item--active">
            Financeiro
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('perfil')}>
            Perfil
          </button>
          <button type="button" className="menu-item" onClick={() => onNavigate('tarefas')}>
            Tarefas
          </button>
          {currentUser.perfil === 'ADMINISTRADOR' ? (
            <button type="button" className="menu-item" onClick={() => onNavigate('configuracoes')}>
              ⚙ Configurações
            </button>
          ) : null}
        </nav>
        <div className="sidebar-user">
          <strong>{currentUser.nome}</strong>
          <span>{currentUser.email}</span>
          <button type="button" className="sidebar-logout" onClick={onLogout}>
            Sair
          </button>
        </div>
      </aside>

      <section className="animals-content">
        <ModuleHeader
          icon="wallet"
          title="Financeiro"
          description="Documentos, aprovações e resultados reunidos para uma conferência rápida e segura."
          metrics={[
            { value: '5', label: 'Áreas integradas' },
            { value: currentUser.perfil === 'FINANCEIRO' ? 'Financeiro' : 'Gestor', label: 'Seu acesso' },
          ]}
        />

        <div className="insumos-tabs">
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'nfs' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('nfs')}
          >
            NFs
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'aprovacoes' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('aprovacoes')}
          >
            Aprovações
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'parceiros' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('parceiros')}
          >
            Parceiros
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'funcionarios' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('funcionarios')}
          >
            Funcionários
          </button>
          <button
            type="button"
            className={`insumos-tab ${activeTab === 'dashboard' ? 'insumos-tab--active' : ''}`}
            onClick={() => setActiveTab('dashboard')}
          >
            Dashboard
          </button>
        </div>

        {activeTab === 'nfs' ? <NotasFiscaisTab currentUser={currentUser} /> : null}
        {activeTab === 'aprovacoes' ? <AprovacoesTab currentUser={currentUser} /> : null}
        {activeTab === 'parceiros' ? <ParceirosTab currentUser={currentUser} /> : null}
        {activeTab === 'funcionarios' ? <FuncionariosTab currentUser={currentUser} /> : null}
        {activeTab === 'dashboard' ? <DashboardTab currentUser={currentUser} /> : null}
      </section>
    </main>
  )
}

export default FinanceiroPage
