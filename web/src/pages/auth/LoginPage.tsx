import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { http, apiError } from '../../lib/api'
import { setSession } from '../../lib/auth'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'

export default function LoginPage() {
  const nav = useNavigate()
  const [form, setForm] = useState({ email: '', password: '', tenantSlug: '' })
  const [isSuperAdmin, setIsSuperAdmin] = useState(false)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function submit(e: FormEvent) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      if (isSuperAdmin) {
        const { data } = await http.post('/auth/super/login', { email: form.email, password: form.password })
        const d = data.data
        setSession(d.accessToken, { userId: d.userId, tenantId: '', roles: d.roles ?? ['SUPER_ADMIN'], email: form.email })
        nav('/admin')
      } else {
        const { data } = await http.post('/auth/login', form)
        const d = data.data
        setSession(d.accessToken, { userId: d.userId, tenantId: d.tenantId, roles: d.roles ?? [], email: form.email })
        nav('/')
      }
    } catch (err) {
      setError(apiError(err))
    } finally {
      setLoading(false)
    }
  }

  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement>) => setForm(f => ({ ...f, [k]: e.target.value }))

  return (
    <div className="min-h-screen bg-[var(--color-bg)] flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-sm p-8">
        <div className="text-center mb-8">
          <img src="/labix_logo.png" alt="LABIX" className="h-12 w-auto mx-auto mb-3" />
          <p className="text-sm text-[var(--color-muted)]">Lab management platform</p>
        </div>

        <form onSubmit={submit} className="flex flex-col gap-4">
          <Input label="Email" type="email" required value={form.email} onChange={set('email')} placeholder="you@example.com" />
          <Input label="Password" type="password" required value={form.password} onChange={set('password')} placeholder="••••••••" />

          {!isSuperAdmin && (
            <Input label="Lab ID (slug)" required value={form.tenantSlug} onChange={set('tenantSlug')} placeholder="my-lab" />
          )}

          {error && <p className="text-sm text-[var(--color-red)] bg-red-50 rounded-lg px-3 py-2">{error}</p>}

          <Button type="submit" size="lg" loading={loading} className="w-full mt-2">
            Sign in
          </Button>
        </form>

        <button
          onClick={() => setIsSuperAdmin(v => !v)}
          className="mt-5 w-full text-xs text-[var(--color-muted)] hover:text-[var(--color-navy)] text-center"
        >
          {isSuperAdmin ? '← Sign in as lab staff' : 'LABIX team sign in →'}
        </button>
      </div>
    </div>
  )
}
