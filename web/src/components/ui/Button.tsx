import { ButtonHTMLAttributes, forwardRef } from 'react'

type Variant = 'primary' | 'secondary' | 'ghost' | 'danger'
type Size = 'sm' | 'md' | 'lg'

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  loading?: boolean
}

const base = 'inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-1 disabled:opacity-50 disabled:pointer-events-none'

const variants: Record<Variant, string> = {
  primary:   'bg-[var(--color-navy)] text-white hover:bg-[var(--color-navy-light)] focus-visible:ring-[var(--color-navy)]',
  secondary: 'border border-[var(--color-border)] bg-white text-[var(--color-text)] hover:bg-[var(--color-bg)] focus-visible:ring-[var(--color-navy)]',
  ghost:     'text-[var(--color-muted)] hover:bg-[var(--color-bg)] hover:text-[var(--color-text)] focus-visible:ring-[var(--color-navy)]',
  danger:    'bg-[var(--color-red)] text-white hover:bg-red-700 focus-visible:ring-[var(--color-red)]',
}

const sizes: Record<Size, string> = {
  sm: 'h-8 px-3 text-sm',
  md: 'h-9 px-4 text-sm',
  lg: 'h-11 px-6 text-base',
}

const Button = forwardRef<HTMLButtonElement, Props>(
  ({ variant = 'primary', size = 'md', loading, children, disabled, className = '', ...rest }, ref) => (
    <button
      ref={ref}
      disabled={disabled || loading}
      className={`${base} ${variants[variant]} ${sizes[size]} ${className}`}
      {...rest}
    >
      {loading && <span className="h-4 w-4 animate-spin rounded-full border-2 border-current border-t-transparent" />}
      {children}
    </button>
  ),
)
Button.displayName = 'Button'
export default Button
