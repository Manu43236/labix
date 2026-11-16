import { createContext, useContext, useState, useCallback, ReactNode } from 'react'

type ToastType = 'success' | 'error' | 'info'
interface Toast { id: number; msg: string; type: ToastType }
interface Ctx { toast: (msg: string, type?: ToastType) => void }

const ToastCtx = createContext<Ctx>({ toast: () => {} })

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  let next = 0

  const toast = useCallback((msg: string, type: ToastType = 'info') => {
    const id = next++
    setToasts(t => [...t, { id, msg, type }])
    setTimeout(() => setToasts(t => t.filter(x => x.id !== id)), 4000)
  }, [])

  const bg: Record<ToastType, string> = {
    success: 'bg-[var(--color-green)] text-white',
    error:   'bg-[var(--color-red)] text-white',
    info:    'bg-[var(--color-navy)] text-white',
  }

  return (
    <ToastCtx.Provider value={{ toast }}>
      {children}
      <div className="fixed bottom-4 right-4 flex flex-col gap-2 z-[100]">
        {toasts.map(t => (
          <div key={t.id} className={`rounded-lg px-4 py-3 shadow-lg text-sm max-w-sm ${bg[t.type]}`}>
            {t.msg}
          </div>
        ))}
      </div>
    </ToastCtx.Provider>
  )
}

export const useToast = () => useContext(ToastCtx)
