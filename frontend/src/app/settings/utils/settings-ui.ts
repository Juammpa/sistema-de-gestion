// Clases reutilizadas en las secciones de Ajustes (Tailwind las detecta igual desde un .ts)
const BUTTON =
  'inline-flex cursor-pointer items-center justify-center gap-2 rounded-xl px-5 py-3 text-sm font-semibold transition-ui duration-150 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-panel-to motion-safe:active:scale-[0.98]';

export const SETTINGS_UI = {
  label: 'mb-1.5 block text-sm font-semibold text-slate-200',
  help: 'mt-1.5 text-xs text-slate-500',
  field:
    'w-full rounded-xl border border-slate-700/70 bg-canvas px-3.5 py-2.5 text-base text-slate-100 outline-none transition-colors duration-150 placeholder:text-slate-500 hover:border-slate-600 focus:border-accent-500 focus:ring-4 focus:ring-accent-500/15 disabled:cursor-not-allowed disabled:text-slate-500 disabled:opacity-70 sm:text-sm',
  primaryButton: `${BUTTON} bg-accent-600 text-white shadow-lg shadow-accent-600/20 hover:bg-accent-500 focus-visible:ring-accent-300/80`,
  outlineButton: `${BUTTON} border border-slate-700/70 bg-slate-800/50 text-slate-300 hover:bg-slate-800 hover:text-slate-50 focus-visible:ring-slate-400/60`,
  dangerOutlineButton: `${BUTTON} border border-red-500/30 bg-red-500/10 text-red-300 hover:bg-red-500/20 hover:text-red-200 focus-visible:ring-red-400/70`,
  dangerSolidButton: `${BUTTON} bg-red-600 text-white shadow-lg shadow-red-600/25 hover:bg-red-500 focus-visible:ring-red-300/80`,
  ghostDangerButton:
    'cursor-pointer rounded-md px-1 py-1.5 text-sm font-semibold text-red-400 transition-colors hover:text-red-300 hover:underline focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-400/70',
} as const;
