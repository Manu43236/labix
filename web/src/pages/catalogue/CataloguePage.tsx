import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { http } from '../../lib/api'
import { getUser, hasRole } from '../../lib/auth'
import Input from '../../components/ui/Input'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import StatusChip from '../../components/ui/StatusChip'
import { paise } from '../../lib/fmt'

interface LabTest {
  id: string; code: string; name: string; category: string
  sampleType: string; pricePaise: number; active: boolean
}

export default function CataloguePage() {
  const user = getUser()
  const [q, setQ] = useState('')
  const [page, setPage] = useState(0)

  const { data, isLoading } = useQuery({
    queryKey: ['catalogue', q, page],
    queryFn: () => http.get('/catalogue/tests', { params: { q: q || undefined, page, size: 25 } }).then(r => r.data.data),
  })

  const tests: LabTest[] = data?.items ?? []

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-[var(--color-text)]">Test Catalogue</h1>
      </div>

      <Input placeholder="Search tests…" value={q} onChange={e => { setQ(e.target.value); setPage(0) }} />

      <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
        <Table>
          <Thead>
            <tr>
              <Th>Code</Th><Th>Name</Th><Th>Category</Th><Th>Sample</Th><Th>Price</Th><Th>Status</Th>
            </tr>
          </Thead>
          <Tbody>
            {isLoading ? (
              <tr><Td colSpan={6} className="text-center text-[var(--color-muted)] py-8">Loading…</Td></tr>
            ) : tests.length === 0 ? (
              <tr><Td colSpan={6} className="text-center text-[var(--color-muted)] py-8">No tests found</Td></tr>
            ) : tests.map((t: LabTest) => (
              <tr key={t.id} className="hover:bg-[var(--color-bg)]">
                <Td className="font-mono text-xs">{t.code}</Td>
                <Td className="font-medium">{t.name}</Td>
                <Td className="text-[var(--color-muted)]">{t.category}</Td>
                <Td>{t.sampleType}</Td>
                <Td>{paise(t.pricePaise)}</Td>
                <Td><StatusChip status={t.active ? 'ACTIVE' : 'SUSPENDED'} /></Td>
              </tr>
            ))}
          </Tbody>
        </Table>
      </div>

      {data?.totalPages > 1 && (
        <div className="flex gap-2 justify-end">
          <button onClick={() => setPage(p => p - 1)} disabled={page === 0}
            className="px-3 py-1.5 rounded border border-[var(--color-border)] text-sm disabled:opacity-40">Prev</button>
          <span className="text-sm text-[var(--color-muted)] flex items-center">Page {page + 1} / {data.totalPages}</span>
          <button onClick={() => setPage(p => p + 1)} disabled={page >= data.totalPages - 1}
            className="px-3 py-1.5 rounded border border-[var(--color-border)] text-sm disabled:opacity-40">Next</button>
        </div>
      )}
    </div>
  )
}
