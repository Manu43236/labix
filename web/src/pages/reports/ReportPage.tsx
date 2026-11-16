import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { http, apiError } from '../../lib/api'
import { getUser, hasRole } from '../../lib/auth'
import Button from '../../components/ui/Button'
import StatusChip from '../../components/ui/StatusChip'
import Card, { CardHeader, CardBody } from '../../components/ui/Card'
import { useToast } from '../../components/ui/Toast'
import { dateTime } from '../../lib/fmt'

interface Report {
  id: string; visitId: string; version: number; status: string
  verifiedBy?: string; verifiedAt?: string; releasedAt?: string; hasPdf: boolean
}

export default function ReportPage() {
  const { visitId } = useParams<{ visitId: string }>()
  const qc = useQueryClient()
  const nav = useNavigate()
  const { toast } = useToast()
  const user = getUser()
  const canManage = hasRole(user, 'LAB_ADMIN', 'PATHOLOGIST')

  const { data: report, isLoading } = useQuery<Report>({
    queryKey: ['report', visitId],
    queryFn: () => http.get(`/visits/${visitId}/report`).then(r => r.data.data),
    retry: false,
  })

  const createMut = useMutation({
    mutationFn: () => http.post(`/visits/${visitId}/report`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['report', visitId] }); toast('Draft created', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const verifyMut = useMutation({
    mutationFn: (id: string) => http.put(`/reports/${id}/verify`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['report', visitId] }); toast('Report verified', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const releaseMut = useMutation({
    mutationFn: (id: string) => http.put(`/reports/${id}/release`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['report', visitId] }); toast('Report released', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  const correctMut = useMutation({
    mutationFn: (id: string) => http.post(`/reports/${id}/correction`),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['report', visitId] }); toast('Correction draft created', 'success') },
    onError: err => toast(apiError(err), 'error'),
  })

  function downloadPdf(id: string) {
    http.get(`/reports/${id}/pdf`, { responseType: 'blob' }).then(res => {
      const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const a = document.createElement('a')
      a.href = url; a.download = `report-v${report?.version}.pdf`; a.click()
      URL.revokeObjectURL(url)
    }).catch(err => toast(apiError(err), 'error'))
  }

  if (isLoading) return <div className="text-[var(--color-muted)]">Loading…</div>

  return (
    <div className="max-w-2xl flex flex-col gap-5">
      <div className="flex items-center gap-3">
        <Button variant="ghost" onClick={() => nav(-1)}>←</Button>
        <h1 className="text-xl font-semibold">Report — Visit</h1>
      </div>

      {!report ? (
        <Card>
          <CardBody className="flex flex-col items-center gap-4 py-10">
            <p className="text-[var(--color-muted)]">No report exists for this visit.</p>
            {canManage && (
              <Button onClick={() => createMut.mutate()} loading={createMut.isPending}>Create Draft</Button>
            )}
          </CardBody>
        </Card>
      ) : (
        <Card>
          <CardHeader className="flex items-center justify-between">
            <div>
              <h2 className="font-semibold">Report v{report.version}</h2>
              {report.verifiedAt && <p className="text-xs text-[var(--color-muted)] mt-0.5">Verified {dateTime(report.verifiedAt)}</p>}
              {report.releasedAt && <p className="text-xs text-[var(--color-muted)]">Released {dateTime(report.releasedAt)}</p>}
            </div>
            <StatusChip status={report.status} size="md" />
          </CardHeader>
          <CardBody>
            <div className="flex gap-3 flex-wrap">
              {canManage && report.status === 'DRAFT' && (
                <Button onClick={() => verifyMut.mutate(report.id)} loading={verifyMut.isPending}>
                  Verify Report
                </Button>
              )}
              {canManage && report.status === 'VERIFIED' && (
                <Button onClick={() => releaseMut.mutate(report.id)} loading={releaseMut.isPending}>
                  Release & Generate PDF
                </Button>
              )}
              {report.hasPdf && (
                <Button variant="secondary" onClick={() => downloadPdf(report.id)}>
                  Download PDF
                </Button>
              )}
              {canManage && report.status === 'RELEASED' && (
                <Button variant="secondary" onClick={() => correctMut.mutate(report.id)} loading={correctMut.isPending}>
                  Create Correction
                </Button>
              )}
              {canManage && report.status === 'DRAFT' && !report.hasPdf && (
                <p className="text-sm text-[var(--color-muted)] self-center">
                  Verify the report first, then release to generate the PDF.
                </p>
              )}
            </div>
          </CardBody>
        </Card>
      )}
    </div>
  )
}
