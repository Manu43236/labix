interface Props { flag: string | null | undefined }

const map: Record<string, string> = {
  CRITICAL: 'bg-[var(--flag-critical-bg)] text-[var(--flag-critical-fg)] font-bold',
  HIGH:     'bg-[var(--flag-high-bg)] text-[var(--flag-high-fg)] font-semibold',
  LOW:      'bg-[var(--flag-low-bg)] text-[var(--flag-low-fg)] font-semibold',
  NORMAL:   'text-[var(--flag-normal-fg)]',
}

const icon: Record<string, string> = {
  CRITICAL: '⚠',
  HIGH:     '↑',
  LOW:      '↓',
  NORMAL:   '',
}

export default function FlagChip({ flag }: Props) {
  if (!flag) return <span className="text-[var(--color-muted)] text-xs">—</span>
  const cls = map[flag] ?? 'text-[var(--color-muted)]'
  return (
    <span className={`inline-flex items-center gap-0.5 rounded px-1.5 py-0.5 text-xs ${cls}`}>
      {icon[flag] && <span aria-hidden>{icon[flag]}</span>}
      {flag}
    </span>
  )
}
