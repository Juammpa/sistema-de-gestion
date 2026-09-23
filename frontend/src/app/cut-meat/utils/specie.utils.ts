export type SpecieValue = 'BOVINE' | 'PORCINE';

const SPECIE_LABELS: Record<SpecieValue, string> = {
  BOVINE: 'Bovino',
  PORCINE: 'Porcino',
};

// Opciones listas para el <select>
export const SPECIE_OPTIONS: { value: SpecieValue; label: string }[] = (
  Object.keys(SPECIE_LABELS) as SpecieValue[]
).map((value) => ({ value, label: SPECIE_LABELS[value] }));

// Traduce el valor recibido del backend para mostrarlo
export default function translateSpecie(specie: string): string {
  return SPECIE_LABELS[specie as SpecieValue] ?? specie;
}
