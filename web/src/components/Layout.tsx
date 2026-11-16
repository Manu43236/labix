import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { getUser, clearSession, hasRole } from '../lib/auth'
import { S } from '../lib/strings'

const navItem = 'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors'
const active  = 'bg-[var(--color-teal)] text-white'
const idle    = 'text-slate-300 hover:bg-white/10 hover:text-white'

export default function Layout() {
  const user = getUser()
  const nav  = useNavigate()

  function logout() {
    clearSession()
    nav('/login')
  }

  const isAdmin   = hasRole(user, 'LAB_ADMIN')
  const isTech    = hasRole(user, 'TECHNICIAN')
  const isPath    = hasRole(user, 'PATHOLOGIST')
  const isRec     = hasRole(user, 'RECEPTIONIST')
  const isSuperAdmin = hasRole(user, 'SUPER_ADMIN')

  return (
    <div className="flex h-screen overflow-hidden bg-[var(--color-bg)]">
      {/* Sidebar */}
      <nav
        style={{ width: 'var(--sidebar-w)' }}
        className="flex flex-col bg-[var(--color-navy)] shrink-0 overflow-y-auto"
      >
        {/* Logo */}
        <div className="px-4 py-4 border-b border-white/10">
          <div className="bg-white rounded-lg px-3 py-1.5 inline-block">
            <img src="/labix_logo.png" alt="LABIX" className="h-7 w-auto" />
          </div>
        </div>

        <div className="flex-1 px-3 py-4 flex flex-col gap-1">
          <NavLink to="/" end className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.dashboard}</NavLink>

          {(isAdmin || isRec || isTech || isPath) && (
            <NavLink to="/patients" className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.patients}</NavLink>
          )}

          {(isAdmin || isRec) && (
            <NavLink to="/visits" className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.visits}</NavLink>
          )}

          {(isAdmin || isTech || isPath) && (
            <NavLink to="/results" className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.results}</NavLink>
          )}

          {(isAdmin || isPath) && (
            <NavLink to="/reports" className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.reports}</NavLink>
          )}

          {(isAdmin || isRec || isPath) && (
            <NavLink to="/catalogue" className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.catalogue}</NavLink>
          )}

          {isAdmin && (
            <NavLink to="/setup" className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.setup}</NavLink>
          )}

          {isSuperAdmin && (
            <NavLink to="/admin" className={({ isActive }) => `${navItem} ${isActive ? active : idle}`}>{S.superAdmin}</NavLink>
          )}
        </div>

        <div className="px-3 py-4 border-t border-white/10">
          <div className="text-xs text-slate-400 px-3 mb-2 truncate">{user?.email}</div>
          <button onClick={logout} className={`${navItem} ${idle} w-full`}>{S.logout}</button>
        </div>
      </nav>

      {/* Main */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Topbar */}
        <header
          style={{ height: 'var(--topbar-h)' }}
          className="shrink-0 flex items-center px-6 bg-white border-b border-[var(--color-border)]"
        >
          <span className="text-sm text-[var(--color-muted)]">
            {user?.roles?.join(' · ')}
          </span>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
