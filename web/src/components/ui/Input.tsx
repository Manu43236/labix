import { InputHTMLAttributes, forwardRef } from 'react'

interface Props extends InputHTMLAttributes<HTMLInputElement> {
  label?: string
  error?: string
}

const Input = forwardRef<HTMLInputElement, Props>(
  ({ label, error, className = '', ...rest }, ref) => (
    <div className="flex flex-col gap-1">
      {label && <label className="text-sm font-medium text-[var(--color-text)]">{label}</label>}
      <input
        ref={ref}
        className={`h-9 rounded-lg border px-3 text-sm bg-white text-[var(--color-text)] outline-none
          transition-colors placeholder:text-[var(--color-muted)]
          border-[var(--color-border)] focus:border-[var(--color-teal)] focus:ring-1 focus:ring-[var(--color-teal)]
          disabled:opacity-50 disabled:bg-[var(--color-bg)]
          ${error ? 'border-[var(--color-red)]' : ''}
          ${className}`}
        {...rest}
      />
      {error && <p className="text-xs text-[var(--color-red)]">{error}</p>}
    </div>
  ),
)
Input.displayName = 'Input'
export default Input
