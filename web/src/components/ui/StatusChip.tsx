interface Props { status: string; size?: 'sm' | 'md' }

const map: Record<string, string> = {
  PENDING:    'bg-[var(--chip-pending-bg)] text-[var(--chip-pending-fg)]',
  COLLECTED:  'bg-[var(--chip-collected-bg)] text-[var(--chip-collected-fg)]',
  RECEIVED:   'bg-[var(--chip-received-bg)] text-[var(--chip-received-fg)]',
  ENTERED:    'bg-[var(--chip-entered-bg)] text-[var(--chip-entered-fg)]',
  VERIFIED:   'bg-[var(--chip-verified-bg)] text-[var(--chip-verified-fg)]',
  RELEASED:   'bg-[var(--chip-released-bg)] text-[var(--chip-released-fg)]',
  PAID:       'bg-[var(--chip-paid-bg)] text-[var(--chip-paid-fg)]',
  DUE:        'bg-[var(--chip-due-bg)] text-[var(--chip-due-fg)]',
  PARTIAL:    'bg-[var(--chip-partial-bg)] text-[var(--chip-partial-fg)]',
  REGISTERED: 'bg-[var(--chip-collected-bg)] text-[var(--chip-collected-fg)]',
  DRAFT:      'bg-gray-100 text-gray-700',
  SUPERSEDED: 'bg-gray-100 text-gray-500',
  REJECTED:   'bg-[var(--chip-due-bg)] text-[var(--chip-due-fg)]',
  ACTIVE:     'bg-[var(--chip-verified-bg)] text-[var(--chip-verified-fg)]',
  SUSPENDED:  'bg-[var(--chip-due-bg)] text-[var(--chip-due-fg)]',
}

export default function StatusChip({ status, size = 'sm' }: Props) {
  const cls = map[status] ?? 'bg-gray-100 text-gray-600'
  return (
    <span className={`inline-block rounded-full font-medium ${size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-sm'} ${cls}`}>
      {status}
    </span>
  )
}
