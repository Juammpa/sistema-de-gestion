export type DayRange = { from: Date | null; to: Date | null };

export const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate());

export const startOfMonth = (d: Date) => new Date(d.getFullYear(), d.getMonth(), 1);

export const daysInMonth = (d: Date) => new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();

export const addDays = (d: Date, n: number) =>
  new Date(d.getFullYear(), d.getMonth(), d.getDate() + n);

// Suma meses sin desbordar: 31 de enero + 1 mes = 28/29 de febrero (no 3 de marzo)
export const addMonths = (d: Date, n: number) => {
  const first = new Date(d.getFullYear(), d.getMonth() + n, 1);
  return new Date(first.getFullYear(), first.getMonth(), Math.min(d.getDate(), daysInMonth(first)));
};

export const isSameDay = (a?: Date | null, b?: Date | null) =>
  !!a &&
  !!b &&
  a.getFullYear() === b.getFullYear() &&
  a.getMonth() === b.getMonth() &&
  a.getDate() === b.getDate();

export const isSameMonth = (a: Date, b: Date) =>
  a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth();

// Clave de un día para el atributo `data-date` ("2026-10-23")
export const dayKey = (d: Date) => `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`;

export const parseDayKey = (key: string) => {
  const [year, month, day] = key.split('-').map(Number);
  return new Date(year, month - 1, day);
};

// Grilla fija de 6 semanas (42 celdas): `null` son los huecos antes y después del mes.
// Un alto constante evita que el calendario salte de tamaño al cambiar de mes.
export const monthCells = (view: Date, weekStartsOn: 0 | 1): (Date | null)[] => {
  const year = view.getFullYear();
  const month = view.getMonth();
  const total = daysInMonth(view);
  const offset = (new Date(year, month, 1).getDay() - weekStartsOn + 7) % 7;
  return Array.from({ length: 42 }, (_, i) => {
    const day = i - offset + 1;
    return day < 1 || day > total ? null : new Date(year, month, day);
  });
};
