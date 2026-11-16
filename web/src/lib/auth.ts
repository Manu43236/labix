export interface AuthUser {
  userId: string
  tenantId: string
  roles: string[]
  name?: string
  email?: string
}

export function getUser(): AuthUser | null {
  try { return JSON.parse(localStorage.getItem('user') ?? 'null') } catch { return null }
}

export function setSession(token: string, user: AuthUser) {
  localStorage.setItem('token', token)
  localStorage.setItem('user', JSON.stringify(user))
}

export function clearSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export function hasRole(user: AuthUser | null, ...roles: string[]): boolean {
  return !!user && roles.some(r => user.roles.includes(r))
}

export function isAdmin(user: AuthUser | null) {
  return hasRole(user, 'LAB_ADMIN')
}
