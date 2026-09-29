const MONTHS_SHORT = [
  'Ene',
  'Feb',
  'Mar',
  'Abr',
  'May',
  'Jun',
  'Jul',
  'Ago',
  'Sep',
  'Oct',
  'Nov',
  'Dic',
];

const MONTHS_LONG = [
  'Enero',
  'Febrero',
  'Marzo',
  'Abril',
  'Mayo',
  'Junio',
  'Julio',
  'Agosto',
  'Septiembre',
  'Octubre',
  'Noviembre',
  'Diciembre',
];

// month: 1 a 12
export const monthShort = (month: number): string => MONTHS_SHORT[month - 1];

export const monthLong = (month: number): string => MONTHS_LONG[month - 1];

// 277000 --> "$277K", 1250000 --> "$1,3M"
export function formatCompact(value: number): string {
  const abs = Math.abs(value);
  const sign = value < 0 ? '-' : '';

  if (abs >= 1_000_000) {
    return `${sign}$${(abs / 1_000_000).toLocaleString('es-AR', { maximumFractionDigits: 1 })}M`;
  }
  if (abs >= 1_000) {
    return `${sign}$${Math.round(abs / 1_000).toLocaleString('es-AR')}K`;
  }
  return `${sign}$${Math.round(abs).toLocaleString('es-AR')}`;
}

// Variacion porcentual (null si no hay base para comparar)
export function pctChange(current: number, previous: number): number | null {
  return previous === 0 ? null : ((current - previous) / Math.abs(previous)) * 100;
}
