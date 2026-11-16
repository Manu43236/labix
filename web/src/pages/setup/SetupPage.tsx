import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { http, apiError } from '../../lib/api'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import Modal from '../../components/ui/Modal'
import StatusChip from '../../components/ui/StatusChip'
import { useToast } from '../../components/ui/Toast'

interface Branch { id: string; name: string; main: boolean }
interface LabUser { id: string; name: string; email: string; active: boolean; roles: string[] }

function BranchesPanel() {
  const qc = useQueryClient()
  const { toast } = useToast()
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ name: '', isMain: false })

  const { data: branches } = useQuery<Branch[]>({
    queryKey: ['branches'],
    queryFn: () => http.get('/branches').then(r => r.data.data ?? []),
  })

  const createMut = useMutation({
    mutationFn: (body: object) => http.post('/branches', body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['branches'] }); setModal(false); toast('Branch created', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  return (
    <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
      <div className="px-5 py-4 border-b border-[var(--color-border)] flex items-center justify-between">
        <h2 className="font-semibold">Branches</h2>
        <Button size="sm" onClick={() => setModal(true)}>+ Add Branch</Button>
      </div>
      <Table>
        <Thead><tr><Th>Name</Th><Th>Main</Th></tr></Thead>
        <Tbody>
          {(branches ?? []).map((b: Branch) => (
            <tr key={b.id}><Td className="font-medium">{b.name}</Td><Td>{b.main ? '✓' : ''}</Td></tr>
          ))}
        </Tbody>
      </Table>
      <Modal open={modal} title="New Branch" onClose={() => setModal(false)}>
        <div className="flex flex-col gap-4">
          <Input label="Name" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.isMain} onChange={e => setForm(f => ({ ...f, isMain: e.target.checked }))} />
            Main branch
          </label>
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setModal(false)}>Cancel</Button>
            <Button loading={createMut.isPending} onClick={() => createMut.mutate(form)}>Save</Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

function UsersPanel() {
  const qc = useQueryClient()
  const { toast } = useToast()
  const [modal, setModal] = useState(false)
  const [form, setForm] = useState({ name: '', email: '', password: '', roles: ['RECEPTIONIST'] })

  const { data: users } = useQuery<LabUser[]>({
    queryKey: ['users'],
    queryFn: () => http.get('/users').then(r => r.data.data?.items ?? []),
  })

  const createMut = useMutation({
    mutationFn: (body: object) => http.post('/users', body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['users'] }); setModal(false); toast('User created', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const toggleMut = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      http.put(`/users/${id}`, { active }),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['users'] }),
    onError: err => toast(apiError(err), 'error'),
  })

  const ROLES = ['LAB_ADMIN', 'RECEPTIONIST', 'TECHNICIAN', 'PATHOLOGIST', 'COLLECTOR']

  function toggleRole(role: string) {
    setForm(f => ({
      ...f,
      roles: f.roles.includes(role) ? f.roles.filter(r => r !== role) : [...f.roles, role],
    }))
  }

  return (
    <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
      <div className="px-5 py-4 border-b border-[var(--color-border)] flex items-center justify-between">
        <h2 className="font-semibold">Users</h2>
        <Button size="sm" onClick={() => setModal(true)}>+ Add User</Button>
      </div>
      <Table>
        <Thead><tr><Th>Name</Th><Th>Email</Th><Th>Roles</Th><Th>Status</Th><Th></Th></tr></Thead>
        <Tbody>
          {(users ?? []).map((u: LabUser) => (
            <tr key={u.id}>
              <Td className="font-medium">{u.name}</Td>
              <Td className="text-[var(--color-muted)]">{u.email}</Td>
              <Td><div className="flex gap-1 flex-wrap">{(u.roles ?? []).map(r => <StatusChip key={r} status={r} />)}</div></Td>
              <Td><StatusChip status={u.active ? 'ACTIVE' : 'SUSPENDED'} /></Td>
              <Td>
                <button onClick={() => toggleMut.mutate({ id: u.id, active: !u.active })}
                  className="text-xs text-[var(--color-teal)] hover:underline">
                  {u.active ? 'Deactivate' : 'Activate'}
                </button>
              </Td>
            </tr>
          ))}
        </Tbody>
      </Table>
      <Modal open={modal} title="New User" onClose={() => setModal(false)}>
        <div className="flex flex-col gap-4">
          <Input label="Name" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
          <Input label="Email" type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} />
          <Input label="Password" type="password" value={form.password} onChange={e => setForm(f => ({ ...f, password: e.target.value }))} />
          <div className="flex flex-col gap-2">
            <label className="text-sm font-medium">Roles</label>
            <div className="flex flex-wrap gap-2">
              {ROLES.map(r => (
                <label key={r} className="flex items-center gap-1.5 text-sm">
                  <input type="checkbox" checked={form.roles.includes(r)} onChange={() => toggleRole(r)} className="accent-[var(--color-teal)]" />
                  {r}
                </label>
              ))}
            </div>
          </div>
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={() => setModal(false)}>Cancel</Button>
            <Button loading={createMut.isPending} onClick={() => createMut.mutate(form)}>Save</Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}

export default function SetupPage() {
  return (
    <div className="max-w-3xl flex flex-col gap-6">
      <h1 className="text-xl font-semibold text-[var(--color-text)]">Setup</h1>
      <BranchesPanel />
      <UsersPanel />
    </div>
  )
}
