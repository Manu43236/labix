import Card, { CardBody } from '../../components/ui/Card'
import { getUser } from '../../lib/auth'

function Stat({ label, value, color }: { label: string; value: string | number; color: string }) {
  return (
    <Card>
      <CardBody className="flex flex-col gap-1">
        <p className="text-xs font-medium text-[var(--color-muted)] uppercase tracking-wide">{label}</p>
        <p className={`text-3xl font-bold ${color}`}>{value}</p>
      </CardBody>
    </Card>
  )
}

export default function DashboardPage() {
  const user = getUser()

  return (
    <div className="flex flex-col gap-6">
      <div>
        <h1 className="text-xl font-semibold text-[var(--color-text)]">Dashboard</h1>
        <p className="text-sm text-[var(--color-muted)] mt-1">Welcome back. Here's today at a glance.</p>
      </div>

      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <Stat label="Today's Visits" value="—" color="text-[var(--color-navy)]" />
        <Stat label="Pending Reports" value="—" color="text-[var(--color-amber)]" />
        <Stat label="Collected Today" value="—" color="text-[var(--color-teal)]" />
        <Stat label="Due Payments" value="—" color="text-[var(--color-red)]" />
      </div>

      <Card>
        <CardBody>
          <p className="text-sm text-[var(--color-muted)]">
            Dashboard analytics coming in a future release. Use the sidebar to navigate.
          </p>
        </CardBody>
      </Card>
    </div>
  )
}
