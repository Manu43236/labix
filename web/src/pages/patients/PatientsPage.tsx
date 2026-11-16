import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { http, apiError } from '../../lib/api'
import { getUser, hasRole } from '../../lib/auth'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import Modal from '../../components/ui/Modal'
import { useToast } from '../../components/ui/Toast'

interface Patient {
  id: string; name: string; patientNo: number; phone: string
  gender?: string; ageYears?: number; dob?: string; email?: string
}

function PatientForm({ initial, onSave, onCancel, loading }: {
  initial?: Partial<Patient>
  onSave: (data: Partial<Patient>) => void
  onCancel: () => void
  loading: boolean
}) {
  const [form, setForm] = useState({
    name: initial?.name ?? '',
    phone: initial?.phone ?? '',
    gender: initial?.gender ?? '',
    ageYears: String(initial?.ageYears ?? ''),
    dob: initial?.dob ?? '',
    email: initial?.email ?? '',
  })
  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setForm(f => ({ ...f, [k]: e.target.value }))

  return (
    <div className="flex flex-col gap-4">
      <Input label="Name *" required value={form.name} onChange={set('name')} />
      <Input label="Phone *" required value={form.phone} onChange={set('phone')} type="tel" />
      <div className="flex flex-col gap-1">
        <label className="text-sm font-medium text-[var(--color-text)]">Gender</label>
        <select value={form.gender} onChange={set('gender')}
          className="h-9 rounded-lg border border-[var(--color-border)] px-3 text-sm bg-white">
          <option value="">Select</option>
          <option value="M">Male</option>
          <option value="F">Female</option>
          <option value="O">Other</option>
        </select>
      </div>
      <Input label="Age (years)" type="number" value={form.ageYears} onChange={set('ageYears')} />
      <Input label="Date of Birth" type="date" value={form.dob} onChange={set('dob')} />
      <Input label="Email" type="email" value={form.email} onChange={set('email')} />
      <div className="flex justify-end gap-3 pt-2">
        <Button variant="secondary" onClick={onCancel}>Cancel</Button>
        <Button loading={loading} onClick={() => onSave({
          name: form.name, phone: form.phone,
          gender: form.gender || undefined,
          ageYears: form.ageYears ? Number(form.ageYears) : undefined,
          dob: form.dob || undefined,
          email: form.email || undefined,
        })}>Save</Button>
      </div>
    </div>
  )
}

export default function PatientsPage() {
  const user = getUser()
  const qc = useQueryClient()
  const { toast } = useToast()
  const canWrite = hasRole(user, 'LAB_ADMIN', 'RECEPTIONIST')

  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)
  const [modal, setModal] = useState<'create' | { id: string } | null>(null)

  const { data, isLoading } = useQuery({
    queryKey: ['patients', q, page],
    queryFn: () => http.get('/patients', { params: { q: q || undefined, page, size: 20 } }).then(r => r.data.data),
  })

  const createMut = useMutation({
    mutationFn: (body: Partial<Patient>) => http.post('/patients', body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['patients'] }); setModal(null); toast('Patient created', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const patients: Patient[] = data?.items ?? []

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-[var(--color-text)]">Patients</h1>
        {canWrite && <Button onClick={() => setModal('create')}>+ New Patient</Button>}
      </div>

      <Input placeholder="Search by name or phone…" value={q} onChange={e => { setQ(e.target.value); setPage(0) }} />

      <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
        <Table>
          <Thead>
            <tr>
              <Th>No.</Th><Th>Name</Th><Th>Phone</Th><Th>Gender</Th><Th>Age</Th>
            </tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <tr><Td colSpan={5} className="text-center text-[var(--color-muted)] py-8">Loading…</Td></tr>
            ) : patients.length === 0 ? (
              <tr><Td colSpan={5} className="text-center text-[var(--color-muted)] py-8">No patients found</Td></tr>
            ) : patients.map((p: Patient) => (
              <tr key={p.id} className="hover:bg-[var(--color-bg)] cursor-pointer" onClick={() => setModal({ id: p.id })}>
                <Td className="font-mono text-xs">{p.patientNo}</Td>
                <Td className="font-medium">{p.name}</Td>
                <Td>{p.phone}</Td>
                <Td>{p.gender ?? '—'}</Td>
                <Td>{p.ageYears ? `${p.ageYears}Y` : (p.dob ? `DOB: ${p.dob}` : '—')}</Td>
              </tr>
            ))}
          </Tbody>
        </Table>
      </div>

      {/* Pagination */}
      {data?.totalPages > 1 && (
        <div className="flex gap-2 justify-end">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Prev</Button>
          <span className="text-sm text-[var(--color-muted)] flex items-center">Page {page + 1} / {data.totalPages}</span>
          <Button variant="secondary" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</Button>
        </div>
      )}

      {/* Create modal */}
      {modal === 'create' && (
        <Modal open title="New Patient" onClose={() => setModal(null)}>
          <PatientForm
            onSave={body => createMut.mutate(body)}
            onCancel={() => setModal(null)}
            loading={createMut.isPending}
          />
        </Modal>
      )}
    </div>
  )
}
