import { SearchFilter } from '../interfaces/search-filter.interface';

// Compara texto en español ignorando mayusculas y tildes
const COLLADOR = new Intl.Collator('es', { sensitivity: 'base' });

export const compareText = (a: string, b: string): number => COLLADOR.compare(a, b);

// Valor inicial de cada filtro: El de su primera opcion (la que el <select> muestra al abrir)
export function defaultFilterValues(filters: SearchFilter[]): Record<string, string> {
  return Object.fromEntries(filters.map((filter) => [filter.key, filter.options[0].value]));
}
