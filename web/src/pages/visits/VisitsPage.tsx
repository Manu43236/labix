import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { http } from '../../lib/api'
import { getUser, hasRole } from '../../lib/auth'
import Button from '../../components/ui/Button'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import StatusChip from '../../components/ui/StatusChip'
import { date } from '../../lib/fmt'

interface Visit {
  id: string; visitNo: number; patientName: string; visitDate: string
  visitType: string; status: string; bill?: { totalPaise: number; status: string }
}

export default function VisitsPage() {
  const user = getUser()
  const canCreate = hasRole(user, 'LAB_ADMIN', 'RECEPTIONIST')
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['visits', page],
    queryFn: () => http.get('/visits', { params: { page, size: 20 } }).then(r => r.data.data),
  })

  const visits: Visit[] = data?.items ?? []

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-[var(--color-text)]">Visits</h1>
        {canCreate && <Link to="/visits/new"><Button>+ New Visit</Button></Link>}
      </div>

      <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
        <Table>
          <Thead>
            <tr>
              <Th>Visit #</Th><Th>Patient</Th><Th>Date</Th><Th>Type</Th><Th>Status</Th><Th>Bill</Th><Th></Th>
            </tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <tr><Td colSpan={7} className="text-center text-[var(--color-muted)] py-8">Loading…</Td></tr>
            ) : visits.length === 0 ? (
              <tr><Td colSpan={7} className="text-center text-[var(--color-muted)] py-8">No visits found</Td></tr>
            ) : visits.map((v: Visit) => (
              <tr key={v.id} className="hover:bg-[var(--color-bg)]">
                <Td className="font-mono text-xs">{v.visitNo}</Td>
                <Td className="font-medium">{v.patientName}</Td>
                <Td>{date(v.visitDate)}</Td>
                <Td><StatusChip status={v.visitType} /></Td>
                <Td><StatusChip status={v.status} /></Td>
                <Td>{v.bill ? <StatusChip status={v.bill.status} /> : '—'}</Td>
                <Td>
                  <Link to={`/visits/${v.id}`} className="text-sm text-[var(--color-teal)] hover:underline">View</Link>
                </Td>
              </tr>
            ))}
          </Tbody>
        </Table>
      </div>

      {data?.totalPages > 1 && (
        <div className="flex gap-2 justify-end">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(p => p - 1)}>Prev</Button>
          <span className="text-sm text-[var(--color-muted)] flex items-center">Page {page + 1} / {data.totalPages}</span>
          <Button variant="secondary" size="sm" disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}>Next</Button>
        </div>
      )}
    </div>
  )
}
