import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { http, apiError } from '../../lib/api'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import StatusChip from '../../components/ui/StatusChip'
import Modal from '../../components/ui/Modal'
import { useToast } from '../../components/ui/Toast'

interface Tenant { id: string; name: string; slug: string; status: string; planCode: string }

export default function AdminPage() {
  const qc = useQueryClient()
  const { toast } = useToast()
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ name: '', slug: '', planCode: 'WEB', adminName: '', adminEmail: '', adminPassword: '' })

  const { data, isLoading } = useQuery({
    queryKey: ['admin-tenants'],
    queryFn: () => http.get('/admin/tenants', { params: { size: 50 } }).then(r => r.data.data?.items ?? []),
  })

  const createMut = useMutation({
    mutationFn: async (f: typeof form) => {
      const { data: tenantRes } = await http.post('/admin/tenants', { name: f.name, slug: f.slug, planCode: f.planCode })
      const tenantId = tenantRes.data.id
      await http.post(`/admin/tenants/${tenantId}/users`, {
        name: f.adminName, email: f.adminEmail, password: f.adminPassword,
      })
    },
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['admin-tenants'] }); setModal(false); toast('Tenant created', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const suspendMut = useMutation({
    mutationFn: (id: string) => http.post(`/admin/tenants/${id}/suspend`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-tenants'] }),
    onError: err => toast(apiError(err), 'error'),
  })

  const activateMut = useMutation({
    mutationFn: (id: string) => http.post(`/admin/tenants/${id}/activate`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin-tenants'] }),
    onError: err => toast(apiError(err), 'error'),
  })

  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm(f => ({ ...f, [k]: e.target.value }))

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold">Super Admin — Tenants</h1>
        <Button onClick={() => setModal(true)}>+ Onboard Lab</Button>
      </div>

      <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
        <Table>
          <Thead>
            <tr><Th>Name</Th><Th>Slug</Th><Th>Plan</Th><Th>Status</Th><Th></Th></tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <tr><Td colSpan={5} className="text-center text-[var(--color-muted)] py-8">Loading…</Td></tr>
            ) : (data ?? []).map((t: Tenant) => (
              <tr key={t.id} className="hover:bg-[var(--color-bg)]">
                <Td className="font-medium">{t.name}</Td>
                <Td className="font-mono text-xs">{t.slug}</Td>
                <Td>{t.planCode}</Td>
                <Td><StatusChip status={t.status} /></Td>
                <Td>
                  {t.status === 'ACTIVE'
                    ? <button onClick={() => suspendMut.mutate(t.id)} className="text-xs text-[var(--color-red)] hover:underline">Suspend</button>
                    : <button onClick={() => activateMut.mutate(t.id)} className="text-xs text-[var(--color-green)] hover:underline">Activate</button>
                  }
                </Td>
              </tr>
            ))}
          </Tbody>
        </Table>
      </div>

      <Modal open={modal} title="Onboard New Lab" onClose={() => setModal(false)} width="max-w-xl">
        <div className="flex flex-col gap-4">
          <p className="text-sm text-[var(--color-muted)] font-semibold uppercase tracking-wide">Lab Details</p>
          <Input label="Lab Name" value={form.name} onChange={set('name')} />
          <Input label="Slug (URL ID)" value={form.slug} onChange={set('slug')} placeholder="my-lab" />
          <div className="flex flex-col gap-1">
            <label className="text-sm font-medium">Plan</label>
            <select value={form.planCode} onChange={set('planCode')}
              className="h-9 rounded-lg border border-[var(--color-border)] px-3 text-sm bg-white">
              <option value="WEB">Web Only — ₹1200/mo</option>
              <option value="MOB_COLLECT">Web + Mobile (Collection) — ₹1500/mo</option>
              <option value="MOB_FULL">Web + Mobile (Full) — ₹2000/mo</option>
            </select>
          </div>
          <p className="text-sm text-[var(--color-muted)] font-semibold uppercase tracking-wide mt-2">First Admin</p>
          <Input label="Admin Name" value={form.adminName} onChange={set('adminName')} />
          <Input label="Admin Email" type="email" value={form.adminEmail} onChange={set('adminEmail')} />
          <Input label="Temporary Password" type="password" value={form.adminPassword} onChange={set('adminPassword')} />
        </div>
        <div className="flex justify-end gap-3 mt-6">
          <Button variant="secondary" onClick={() => setModal(false)}>Cancel</Button>
          <Button loading={createMut.isPending} onClick={() => createMut.mutate(form)}>Create Lab</Button>
        </div>
      </Modal>
    </div>
  )
}
