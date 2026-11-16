import { HTMLAttributes, ThHTMLAttributes, TdHTMLAttributes } from 'react'

export function Table({ className = '', ...rest }: HTMLAttributes<HTMLTableElement>) {
  return (
    <div className="w-full overflow-auto">
      <table className={`w-full text-sm border-collapse ${className}`} {...rest} />
    </div>
  )
}

export function Thead(p: HTMLAttributes<HTMLTableSectionElement>) { return <thead className="border-b border-[var(--color-border)] bg-[var(--color-bg)]" {...p} /> }
export function Tbody(p: HTMLAttributes<HTMLTableSectionElement>) { return <tbody className="divide-y divide-[var(--color-border)]" {...p} /> }

export function Th({ className = '', ...rest }: ThHTMLAttributes<HTMLTableCellElement>) {
  return <th className={`px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide text-[var(--color-muted)] ${className}`} {...rest} />
}

export function Td({ className = '', ...rest }: TdHTMLAttributes<HTMLTableCellElement>) {
  return <td className={`px-4 py-3 text-[var(--color-text)] ${className}`} {...rest} />
}
