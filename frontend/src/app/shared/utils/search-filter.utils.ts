import { SearchFilter } from '../interfaces/search-filter.interface';

// Compara texto en español ignorando mayusculas y tildes ("Álvarez" queda junto a "Alvarez")
const COLLADOR = new Intl.Collator('es', { sensitivity: 'base' });

export const compareText = (a: string, b: string): number => COLLADOR.compare(a, b);

// Valor inicial de cada filtro: El de su primera opcion (la que el <select> muestra al abrir)
export function defaultFilterValues(filters: SearchFilter[]): Record<string, string> {
  return Object.fromEntries(filters.map((filter) => [filter.key, filter.options[0].value]));
}

// Minusculas y sin tildes: "Álvarez" --> "alvarez"
export function normalizeText(text: string): string {
  return text
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();
}

// true si TODAS las palabras de la busqueda aparecen en alguno de los campos.
// Vacia --> todo coincide. Los digitos de cada campo tambien se buscan sin separadores.
// ("3442501598" encuentra "3442 501598").
export function matchesSearch(query: string, ...fields: (string | null | undefined)[]): boolean {
  const terms = normalizeText(query).split(/\s+/).filter(Boolean);
  if (terms.length === 0) return true;

  const haystack = fields
    .map((field) => {
      const text = normalizeText(field ?? '');
      return `${text} ${text.replace(/\D/g, '')}`;
    })
    .join(' ');

  return terms.every((term) => haystack.includes(term));
}
