import { Navigate, NavLink, Route, Routes } from 'react-router-dom'
import DashboardPage from './pages/DashboardPage.jsx'
import PersonellerPage from './pages/PersonellerPage.jsx'
import ArabalarPage from './pages/ArabalarPage.jsx'
import GarajPage from './pages/GarajPage.jsx'
import './App.css'

const navigation = [
  { to: '/', label: 'Genel Bakış', icon: '◫', end: true },
  { to: '/personeller', label: 'Personeller', icon: '♙' },
  { to: '/arabalar', label: 'Araçlar', icon: '▰' },
  { to: '/garaj', label: 'Garaj', icon: '▤' },
]

function AppLayout() {
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">K</div>
          <div>
            <strong>Kurumsal</strong>
            <span>Yönetim Paneli</span>
          </div>
        </div>

        <div className="nav-caption">ÇALIŞMA ALANI</div>
        <nav className="sidebar-nav" aria-label="Ana menü">
          {navigation.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              <span className="nav-icon" aria-hidden="true">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <span className="status-dot" />
          <span>API bağlantısı</span>
          <strong>Hazır</strong>
        </div>
      </aside>

      <div className="main-column">
        <header className="topbar">
          <div className="breadcrumb">Kurumsal <span>/</span> Yönetim</div>
          <div className="topbar-user">
            <div className="avatar">YK</div>
            <div><strong>Yönetici</strong><span>Yönetim hesabı</span></div>
          </div>
        </header>
        <main className="page-content">
          <Routes>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/personeller" element={<PersonellerPage />} />
            <Route path="/arabalar" element={<ArabalarPage />} />
            <Route path="/garaj" element={<GarajPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  )
}

export default function App() {
  return <AppLayout />
}
