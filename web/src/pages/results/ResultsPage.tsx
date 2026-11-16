import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { http, apiError } from '../../lib/api'
import { getUser, hasRole } from '../../lib/auth'
import Button from '../../components/ui/Button'
import FlagChip from '../../components/ui/FlagChip'
import { Table, Thead, Tbody, Th, Td } from '../../components/ui/Table'
import { useToast } from '../../components/ui/Toast'

interface ResultRow {
  id: string; parameterId: string; parameterName: string
  unit: string; valueNumber: number | null; valueText: string
  rangeLow: number | null; rangeHigh: number | null; rangeText: string
  flag: string | null; rangeEdited: boolean
}

interface EntryItem { parameterId: string; valueNumber?: number; valueText?: string }

export default function ResultsPage() {
  const { visitId } = useParams<{ visitId: string }>()
  const qc = useQueryClient()
  const nav = useNavigate()
  const { toast } = useToast()
  const user = getUser()
  const canEnter = hasRole(user, 'LAB_ADMIN', 'TECHNICIAN', 'PATHOLOGIST')
  const canEditRange = hasRole(user, 'LAB_ADMIN', 'PATHOLOGIST')

  const [entries, setEntries] = useState<Record<string, string>>({})
  const [rangeEdit, setRangeEdit] = useState<{ id: string; low: string; high: string } | null>(null)

  const { data: results, isLoading } = useQuery<ResultRow[]>({
    queryKey: ['results', visitId],
    queryFn: () => http.get(`/visits/${visitId}/results`).then(r => r.data.data ?? []),
  })

  // We also need parameters to know what to enter (from the visit tests)
  const { data: visit } = useQuery({
    queryKey: ['visit', visitId],
    queryFn: () => http.get(`/visits/${visitId}`).then(r => r.data.data),
  })

  const enterMut = useMutation({
    mutationFn: (items: EntryItem[]) => http.post(`/visits/${visitId}/results`, items),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['results', visitId] }); toast('Results saved', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const rangeMut = useMutation({
    mutationFn: ({ id, ...body }: { id: string; rangeLow?: number; rangeHigh?: number; rangeText?: string }) =>
      http.put(`/results/${id}/range`, body),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['results', visitId] }); setRangeEdit(null); toast('Range updated', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  function saveAll() {
    const items: EntryItem[] = Object.entries(entries)
      .filter(([, v]) => v !== '')
      .map(([parameterId, v]) => ({
        parameterId,
        valueNumber: !isNaN(Number(v)) && v !== '' ? Number(v) : undefined,
        valueText: isNaN(Number(v)) ? v : undefined,
      }))
    if (items.length === 0) return toast('Enter at least one value', 'error')
    enterMut.mutate(items)
  }

  if (isLoading) return <div className="text-[var(--color-muted)]">Loading…</div>

  // Build a merged list: existing results + any parameters from visit tests not yet entered
  const existingMap = new Map((results ?? []).map(r => [r.parameterId, r]))

  // Get all parameter IDs from the visit's tests
  const visitTests = visit?.tests ?? []
  const allParamIds: string[] = visitTests.flatMap((t: { parameterIds?: string[] }) => t.parameterIds ?? [])

  return (
    <div className="max-w-4xl flex flex-col gap-5">
      <div className="flex items-center gap-3">
        <Button variant="ghost" onClick={() => nav(-1)}>←</Button>
        <h1 className="text-xl font-semibold">Results — Visit #{visit?.visitNo}</h1>
      </div>

      {results && results.length > 0 && (
        <div className="bg-white rounded-xl border border-[var(--color-border)] shadow-sm overflow-hidden">
          <Table>
            <Thead>
              <tr>
                <Th>Parameter</Th>
                <Th>Value</Th>
                <Th>Unit</Th>
                <Th>Range</Th>
                <Th>Flag</Th>
                {canEnter && <Th>Edit Value</Th>}
              </tr>
            </Thead>
            <Tbody>
              {results.map((r: ResultRow) => (
                <tr key={r.id} className={r.flag === 'CRITICAL' ? 'bg-red-50' : r.flag && r.flag !== 'NORMAL' ? 'bg-amber-50' : ''}>
                  <Td className="font-medium">{r.parameterName}</Td>
                  <Td className="font-mono font-semibold">
                    {r.valueNumber != null ? r.valueNumber : r.valueText || '—'}
                  </Td>
                  <Td className="text-[var(--color-muted)]">{r.unit || '—'}</Td>
                  <Td className="text-[var(--color-muted)] text-xs">
                    {r.rangeText || '—'}
                    {r.rangeEdited && <span className="ml-1 text-amber-600 text-xs">(edited)</span>}
                    {canEditRange && (
                      <button onClick={() => setRangeEdit({ id: r.id, low: String(r.rangeLow ?? ''), high: String(r.rangeHigh ?? '') })}
                        className="ml-2 text-[var(--color-teal)] hover:underline text-xs">edit</button>
                    )}
                  </Td>
                  <Td><FlagChip flag={r.flag} /></Td>
                  {canEnter && (
                    <Td>
                      <input type="text"
                        className="w-24 h-8 rounded border border-[var(--color-border)] px-2 text-sm"
                        placeholder={r.valueNumber != null ? String(r.valueNumber) : r.valueText || ''}
                        value={entries[r.parameterId] ?? ''}
                        onChange={e => setEntries(es => ({ ...es, [r.parameterId]: e.target.value }))} />
                    </Td>
                  )}
                </tr>
              ))}
            </Tbody>
          </Table>
        </div>
      )}

      {results?.length === 0 && (
        <div className="bg-white rounded-xl border border-[var(--color-border)] p-8 text-center text-[var(--color-muted)]">
          No results entered yet. Add parameter IDs below and save.
        </div>
      )}

      {canEnter && (
        <div className="flex justify-end">
          <Button onClick={saveAll} loading={enterMut.isPending}>Save Results</Button>
        </div>
      )}

      {/* Range edit inline panel */}
      {rangeEdit && (
        <div className="bg-white rounded-xl border border-[var(--color-border)] p-5">
          <h3 className="font-semibold mb-3">Edit Reference Range</h3>
          <div className="flex gap-3 items-end">
            <div className="flex flex-col gap-1">
              <label className="text-xs text-[var(--color-muted)]">Low</label>
              <input type="number" value={rangeEdit.low}
                onChange={e => setRangeEdit(r => r ? { ...r, low: e.target.value } : r)}
                className="w-24 h-9 rounded-lg border border-[var(--color-border)] px-2 text-sm" />
            </div>
            <div className="flex flex-col gap-1">
              <label className="text-xs text-[var(--color-muted)]">High</label>
              <input type="number" value={rangeEdit.high}
                onChange={e => setRangeEdit(r => r ? { ...r, high: e.target.value } : r)}
                className="w-24 h-9 rounded-lg border border-[var(--color-border)] px-2 text-sm" />
            </div>
            <Button size="sm" loading={rangeMut.isPending}
              onClick={() => rangeMut.mutate({
                id: rangeEdit.id,
                rangeLow: rangeEdit.low ? Number(rangeEdit.low) : undefined,
                rangeHigh: rangeEdit.high ? Number(rangeEdit.high) : undefined,
              })}>Apply</Button>
            <Button size="sm" variant="ghost" onClick={() => setRangeEdit(null)}>Cancel</Button>
          </div>
        </div>
      )}
    </div>
  )
}
