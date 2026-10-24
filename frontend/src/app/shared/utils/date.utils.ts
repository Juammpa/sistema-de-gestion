// yyyy-MM-dd en horario local (toISOString() puede correr la fecha un dia por la zona horaria)
export function toIsoDate(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

// "2026-09-24" --> "24/09/2026" (sin pasar por Date, para evitar el desfase de zona horaria)
export function formatIsoDate(iso: string): string {
  const [year, month, day] = iso.split('-');
  return `${day}/${month}/${year}`;
}

// "2026-09-24" --> Date local a las 00:00 (null si el texto vacío o no es una fecha válida)
export function parseIsoDate(iso: string | null | undefined): Date | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(iso ?? '');
  if (!match) return null;
  const [year, month, day] = [Number(match[1]), Number(match[2]), Number(match[3])];
  const date = new Date(year, month - 1, day);
  // Rechaza fechas que JS "corrige" solo (por ejemplo 2026-02-31)
  return date.getMonth() === month - 1 && date.getDate() === day ? date : null;
}

// Fecha de hace 'days' dias
export function daysAgo(days: number): Date {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date;
}
