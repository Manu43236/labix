export const paise = (p: number) => `₹${(p / 100).toFixed(2)}`
export const date = (d: string) => new Date(d).toLocaleDateString('en-IN', { day:'2-digit', month:'short', year:'numeric' })
export const dateTime = (d: string) => new Date(d).toLocaleString('en-IN', { day:'2-digit', month:'short', year:'numeric', hour:'2-digit', minute:'2-digit' })
