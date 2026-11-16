import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { http } from '../../lib/api'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import StatusChip from '../../components/ui/StatusChip'
import Button from '../../components/ui/Button'
import { date } from '../../lib/fmt'

export default function ReportsListPage() {
  const nav = useNavigate()

  // Show visits with pending or released reports
  const { data, isLoading } = useQuery({
    queryKey: ['visits-for-reports'],
    queryFn: () => http.get('/visits', { params: { size: 50 } }).then(r => r.data.data?.items ?? []),
  })

  return (
    <div className="flex flex-col gap-5">
      <h1 className="text-xl font-semibold text-[var(--color-text)]">Reports</h1>
      <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
        <Table>
          <Thead>
            <tr><Th>Visit #</Th><Th>Patient</Th><Th>Date</Th><Th>Visit Status</Th><Th></Th></tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <tr><Td colSpan={5} className="text-center text-[var(--color-muted)] py-8">Loading…</Td></tr>
            ) : (data ?? []).length === 0 ? (
              <tr><Td colSpan={5} className="text-center text-[var(--color-muted)] py-8">No visits found</Td></tr>
            ) : (data ?? []).map((v: { id: string; visitNo: number; patientName: string; visitDate: string; status: string }) => (
              <tr key={v.id} className="hover:bg-[var(--color-bg)]">
                <Td className="font-mono text-xs">{v.visitNo}</Td>
                <Td className="font-medium">{v.patientName}</Td>
                <Td>{date(v.visitDate)}</Td>
                <Td><StatusChip status={v.status} /></Td>
                <Td>
                  <Button size="sm" variant="secondary" onClick={() => nav(`/reports/${v.id}`)}>
                    Report →
                  </Button>
                </Td>
              </tr>
            ))}
          </Tbody>
        </Table>
      </div>
    </div>
  )
}
