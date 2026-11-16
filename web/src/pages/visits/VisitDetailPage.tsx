import { useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { http, apiError } from '../../lib/api'
import { getUser, hasRole } from '../../lib/auth'
import Button from '../../components/ui/Button'
import StatusChip from '../../components/ui/StatusChip'
import Card, { CardHeader, CardBody } from '../../components/ui/Card'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import { useToast } from '../../components/ui/Toast'
import { paise, date } from '../../lib/fmt'

interface Sample { id: string; sampleNo: number; sampleType: string; barcode: string; status: string }
interface BillItem { id: string; description: string; amountPaise: number }
interface BillData {
  id: string; billNo: number; subtotalPaise: number; discountPaise: number
  totalPaise: number; paidPaise: number; status: string; items: BillItem[]
}
interface VisitTest { id: string; testName: string; pricePaise: number; status: string }

export default function VisitDetailPage() {
  const { id } = useParams<{ id: string }>()
  const qc = useQueryClient()
  const nav = useNavigate()
  const { toast } = useToast()
  const user = getUser()
  const canBill = hasRole(user, 'LAB_ADMIN', 'RECEPTIONIST')
  const canSample = hasRole(user, 'LAB_ADMIN', 'RECEPTIONIST', 'TECHNICIAN')

  const [payAmount, setPayAmount] = useState('')
  const [payMode, setPayMode] = useState('CASH')
  const [sampleStatus, setSampleStatus] = useState<Record<string, string>>({})

  const { data: visit } = useQuery({
    queryKey: ['visit', id],
    queryFn: () => http.get(`/visits/${id}`).then(r => r.data.data),
  })

  const { data: bill } = useQuery<BillData>({
    queryKey: ['bill', id],
    queryFn: () => http.get(`/visits/${id}/bill`).then(r => r.data.data),
    enabled: !!id,
  })

  const { data: samples } = useQuery<Sample[]>({
    queryKey: ['samples', id],
    queryFn: () => http.get(`/visits/${id}/samples`).then(r => r.data.data),
    enabled: !!id,
  })

  const payMut = useMutation({
    mutationFn: (body: object) => http.post(`/visits/${id}/payments`, body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['bill', id] }); setPayAmount(''); toast('Payment recorded', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const sampleMut = useMutation({
    mutationFn: ({ sampleId, status }: { sampleId: string; status: string }) =>
      http.put(`/samples/${sampleId}/status`, { status }),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['samples', id] }); toast('Sample updated', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  if (!visit) return <div className="text-[var(--color-muted)]">Loading…</div>

  return (
    <div className="max-w-4xl flex flex-col gap-5">
      <div className="flex items-center gap-3">
        <Button variant="ghost" onClick={() => nav('/visits')}>←</Button>
        <div>
          <h1 className="text-xl font-semibold">Visit #{visit.visitNo} — {visit.patientName}</h1>
          <p className="text-sm text-[var(--color-muted)]">{date(visit.visitDate)} · {visit.visitType}</p>
        </div>
        <StatusChip status={visit.status} size="md" />
      </div>

      <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
        {/* Tests */}
        <Card>
          <CardHeader><h2 className="font-semibold">Tests</h2></CardHeader>
          <CardBody className="p-0">
            <Table>
              <Thead><tr><Th>Test</Th><Th>Price</Th><Th>Status</Th></tr></Thead>
              <Tbody>
                {(visit.tests ?? []).map((t: VisitTest) => (
                  <tr key={t.id}>
                    <Td>{t.testName}</Td>
                    <Td>{paise(t.pricePaise)}</Td>
                    <Td><StatusChip status={t.status} /></Td>
                  </tr>
                ))}
              </Tbody>
            </Table>
          </CardBody>
        </Card>

        {/* Bill */}
        {bill && (
          <Card>
            <CardHeader className="flex items-center justify-between">
              <h2 className="font-semibold">Bill #{bill.billNo}</h2>
              <StatusChip status={bill.status} />
            </CardHeader>
            <CardBody>
              <div className="flex flex-col gap-2 text-sm mb-4">
                <div className="flex justify-between"><span className="text-[var(--color-muted)]">Subtotal</span><span>{paise(bill.subtotalPaise)}</span></div>
                {bill.discountPaise > 0 && <div className="flex justify-between"><span className="text-[var(--color-muted)]">Discount</span><span>−{paise(bill.discountPaise)}</span></div>}
                <div className="flex justify-between font-semibold border-t pt-2"><span>Total</span><span>{paise(bill.totalPaise)}</span></div>
                <div className="flex justify-between text-[var(--color-green)]"><span>Paid</span><span>{paise(bill.paidPaise)}</span></div>
              </div>

              {canBill && bill.status !== 'PAID' && (
                <div className="flex gap-2 items-end">
                  <div className="flex-1">
                    <input type="number" placeholder="Amount (₹)" value={payAmount}
                      onChange={e => setPayAmount(e.target.value)}
                      className="w-full h-9 rounded-lg border border-[var(--color-border)] px-3 text-sm" />
                  </div>
                  <select value={payMode} onChange={e => setPayMode(e.target.value)}
                    className="h-9 rounded-lg border border-[var(--color-border)] px-2 text-sm">
                    <option>CASH</option><option>UPI</option><option>CARD</option>
                  </select>
                  <Button size="sm" onClick={() => {
                    if (!payAmount) return
                    payMut.mutate({ amountPaise: Math.round(Number(payAmount) * 100), mode: payMode })
                  }} loading={payMut.isPending}>Pay</Button>
                </div>
              )}
            </CardBody>
          </Card>
        )}

        {/* Samples */}
        <Card className="lg:col-span-2">
          <CardHeader><h2 className="font-semibold">Samples</h2></CardHeader>
          <CardBody className="p-0">
            <Table>
              <Thead><tr><Th>Sample #</Th><Th>Type</Th><Th>Barcode</Th><Th>Status</Th>{canSample && <Th>Update</Th>}</tr></Thead>
              <Tbody>
                {(samples ?? []).map((s: Sample) => (
                  <tr key={s.id}>
                    <Td className="font-mono text-xs">{s.sampleNo}</Td>
                    <Td>{s.sampleType}</Td>
                    <Td className="font-mono text-xs">{s.barcode}</Td>
                    <Td><StatusChip status={s.status} /></Td>
                    {canSample && (
                      <Td>
                        <div className="flex gap-2">
                          <select
                            value={sampleStatus[s.id] ?? s.status}
                            onChange={e => setSampleStatus(ss => ({ ...ss, [s.id]: e.target.value }))}
                            className="h-8 rounded border border-[var(--color-border)] px-2 text-xs">
                            <option>PENDING</option><option>COLLECTED</option>
                            <option>RECEIVED</option><option>REJECTED</option>
                          </select>
                          <Button size="sm" variant="secondary"
                            onClick={() => sampleMut.mutate({ sampleId: s.id, status: sampleStatus[s.id] ?? s.status })}>
                            Update
                          </Button>
                        </div>
                      </Td>
                    )}
                  </tr>
                ))}
              </Tbody>
            </Table>
          </CardBody>
        </Card>
      </div>

      {/* Links to results/report */}
      <div className="flex gap-3">
        {hasRole(user, 'LAB_ADMIN', 'TECHNICIAN', 'PATHOLOGIST') && (
          <Link to={`/results/${id}`}>
            <Button variant="secondary">View / Enter Results →</Button>
          </Link>
        )}
        {hasRole(user, 'LAB_ADMIN', 'PATHOLOGIST') && (
          <Link to={`/reports/${id}`}>
            <Button variant="secondary">Report →</Button>
          </Link>
        )}
      </div>
    </div>
  )
}
