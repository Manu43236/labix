import { useState, FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { http, apiError } from '../../lib/api'
import Button from '../../components/ui/Button'
import Input from '../../components/ui/Input'
import { useToast } from '../../components/ui/Toast'
import { paise } from '../../lib/fmt'

interface Patient { id: string; name: string; patientNo: number; phone: string }
interface LabTest { id: string; name: string; category: string; pricePaise: number; sampleType: string }
interface Branch { id: string; name: string }

export default function NewVisitPage() {
  const nav = useNavigate()
  const { toast } = useToast()

  const [patientQ, setPatientQ] = useState('')
  const [selectedPatient, setSelectedPatient] = useState<Patient | null>(null)
  const [selectedTests, setSelectedTests] = useState<string[]>([])
  const [branchId, setBranchId] = useState('')
  const [discountPaise, setDiscountPaise] = useState(0)
  const [payNow, setPayNow] = useState(0)

  const { data: patients } = useQuery({
    queryKey: ['patient-search', patientQ],
    queryFn: () => http.get('/patients', { params: { q: patientQ, size: 10 } }).then(r => r.data.data?.items ?? []),
    enabled: patientQ.length >= 2,
  })

  const { data: tests } = useQuery({
    queryKey: ['catalogue-tests'],
    queryFn: () => http.get('/catalogue/tests', { params: { size: 200 } }).then(r => r.data.data?.items ?? []),
  })

  const { data: branches } = useQuery({
    queryKey: ['branches'],
    queryFn: () => http.get('/branches').then(r => r.data.data ?? []),
  })

  const createMut = useMutation({
    mutationFn: (body: object) => http.post('/visits', body),
    onSuccess: (res) => {
      toast('Visit created', 'success')
      nav(`/visits/${res.data.data.id}`)
    },
    onError: err => toast(apiError(err), 'error'),
  })

  function toggleTest(id: string) {
    setSelectedTests(ts => ts.includes(id) ? ts.filter(t => t !== id) : [...ts, id])
  }

  const selectedTestObjects: LabTest[] = (tests ?? []).filter((t: LabTest) => selectedTests.includes(t.id))
  const subtotal = selectedTestObjects.reduce((s: number, t: LabTest) => s + t.pricePaise, 0)
  const total = Math.max(0, subtotal - discountPaise)

  async function submit(e: FormEvent) {
    e.preventDefault()
    if (!selectedPatient) return toast('Select a patient', 'error')
    if (selectedTests.length === 0) return toast('Select at least one test', 'error')
    if (!branchId) return toast('Select a branch', 'error')

    const body: Record<string, unknown> = {
      patientId: selectedPatient.id,
      branchId,
      testIds: selectedTests,
      discountPaise,
    }
    if (payNow > 0) body.paymentPaise = payNow
    createMut.mutate(body)
  }

  return (
    <div className="max-w-3xl flex flex-col gap-6">
      <div className="flex items-center gap-3">
        <Button variant="ghost" onClick={() => nav(-1)}>←</Button>
        <h1 className="text-xl font-semibold text-[var(--color-text)]">New Visit</h1>
      </div>

      <form onSubmit={submit} className="flex flex-col gap-6">
        {/* Patient */}
        <div className="bg-white rounded-xl border border-[var(--color-border)] p-5">
          <h2 className="font-semibold mb-4">Patient</h2>
          {selectedPatient ? (
            <div className="flex items-center justify-between bg-[var(--color-bg)] rounded-lg px-4 py-3">
              <div>
                <p className="font-medium">{selectedPatient.name}</p>
                <p className="text-sm text-[var(--color-muted)]">{selectedPatient.phone} · #{selectedPatient.patientNo}</p>
              </div>
              <Button variant="ghost" size="sm" onClick={() => setSelectedPatient(null)}>Change</Button>
            </div>
          ) : (
            <div>
              <Input placeholder="Search by name or phone…" value={patientQ} onChange={e => setPatientQ(e.target.value)} />
              {patients && patients.length > 0 && (
                <div className="mt-2 border border-[var(--color-border)] rounded-lg overflow-hidden">
                  {patients.map((p: Patient) => (
                    <button key={p.id} type="button"
                      className="w-full px-4 py-2.5 text-left hover:bg-[var(--color-bg)] border-b border-[var(--color-border)] last:border-0"
                      onClick={() => { setSelectedPatient(p); setPatientQ('') }}>
                      <span className="font-medium">{p.name}</span>
                      <span className="text-sm text-[var(--color-muted)] ml-2">{p.phone}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Branch */}
        <div className="bg-white rounded-xl border border-[var(--color-border)] p-5">
          <h2 className="font-semibold mb-4">Branch</h2>
          <select value={branchId} onChange={e => setBranchId(e.target.value)}
            className="h-9 w-full rounded-lg border border-[var(--color-border)] px-3 text-sm bg-white">
            <option value="">Select branch</option>
            {(branches ?? []).map((b: Branch) => (
              <option key={b.id} value={b.id}>{b.name}</option>
            ))}
          </select>
        </div>

        {/* Tests */}
        <div className="bg-white rounded-xl border border-[var(--color-border)] p-5">
          <h2 className="font-semibold mb-4">Tests</h2>
          <div className="flex flex-col gap-1 max-h-80 overflow-y-auto">
            {(tests ?? []).map((t: LabTest) => (
              <label key={t.id} className="flex items-center gap-3 px-3 py-2 rounded-lg hover:bg-[var(--color-bg)] cursor-pointer">
                <input type="checkbox" checked={selectedTests.includes(t.id)} onChange={() => toggleTest(t.id)}
                  className="accent-[var(--color-teal)]" />
                <span className="flex-1 text-sm font-medium">{t.name}</span>
                <span className="text-sm text-[var(--color-muted)]">{t.category}</span>
                <span className="text-sm font-semibold text-[var(--color-navy)]">{paise(t.pricePaise)}</span>
              </label>
            ))}
          </div>
        </div>

        {/* Bill summary */}
        <div className="bg-white rounded-xl border border-[var(--color-border)] p-5">
          <h2 className="font-semibold mb-4">Bill</h2>
          <div className="flex flex-col gap-3">
            <div className="flex justify-between text-sm">
              <span className="text-[var(--color-muted)]">Subtotal</span>
              <span className="font-medium">{paise(subtotal)}</span>
            </div>
            <div className="flex items-center justify-between gap-4">
              <span className="text-sm text-[var(--color-muted)]">Discount (₹)</span>
              <input type="number" min={0} max={subtotal / 100} step="0.01"
                className="w-28 h-8 rounded-lg border border-[var(--color-border)] px-2 text-sm text-right"
                value={discountPaise / 100}
                onChange={e => setDiscountPaise(Math.round(Number(e.target.value) * 100))} />
            </div>
            <div className="flex justify-between text-sm font-semibold border-t border-[var(--color-border)] pt-3">
              <span>Total</span>
              <span className="text-[var(--color-navy)]">{paise(total)}</span>
            </div>
            <div className="flex items-center justify-between gap-4">
              <span className="text-sm text-[var(--color-muted)]">Pay now (₹)</span>
              <input type="number" min={0} max={total / 100} step="0.01"
                className="w-28 h-8 rounded-lg border border-[var(--color-border)] px-2 text-sm text-right"
                value={payNow / 100}
                onChange={e => setPayNow(Math.round(Number(e.target.value) * 100))} />
            </div>
          </div>
        </div>

        <div className="flex justify-end gap-3">
          <Button variant="secondary" onClick={() => nav(-1)}>Cancel</Button>
          <Button type="submit" loading={createMut.isPending}>Create Visit</Button>
        </div>
      </form>
    </div>
  )
}
