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

// Fecha de hace 'days' dias
export function daysAgo(days: number): Date {
  const date = new Date();
  date.setDate(date.getDate() - days);
  return date;
}
