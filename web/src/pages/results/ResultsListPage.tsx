import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { http } from '../../lib/api'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import Button from '../../components/ui/Button'
import StatusChip from '../../components/ui/StatusChip'
import { date } from '../../lib/fmt'

interface Visit { id: string; patientName: string; visitDate: string; status: string }

export default function ResultsListPage() {
  const nav = useNavigate()

  const { data: visits = [], isLoading } = useQuery<Visit[]>({
    queryKey: ['results-visits'],
    queryFn: () => http.get('/visits', { params: { status: 'IN_PROGRESS', size: 50 } })
      .then(r => r.data.data?.items ?? []),
  })

  return (
    <div className="flex flex-col gap-5">
      <h1 className="text-xl font-semibold">Results Entry</h1>

      <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
        <Table>
          <Thead>
            <tr><Th>Patient</Th><Th>Date</Th><Th>Status</Th><Th></Th></tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <tr><Td colSpan={4} className="text-center text-[var(--color-muted)] py-8">Loading…</Td></tr>
            ) : visits.length === 0 ? (
              <tr><Td colSpan={4} className="text-center text-[var(--color-muted)] py-8">No visits pending results entry.</Td></tr>
            ) : visits.map((v: Visit) => (
              <tr key={v.id} className="hover:bg-[var(--color-bg)]">
                <Td className="font-medium">{v.patientName}</Td>
                <Td>{date(v.visitDate)}</Td>
                <Td><StatusChip status={v.status} /></Td>
                <Td><Button size="sm" onClick={() => nav(`/results/${v.id}`)}>Enter Results</Button></Td>
              </tr>
            ))}
          </Tbody>
        </Table>
      </div>
    </div>
  )
}
