import { AvailableCut } from '../../../stock/interfaces/stock.interface';
import { toCents } from '../../../stock/utils/kilograms.utils';

// Unidades de un corte, en el orden recibido (la mas antigua primero)
export const unitsOfCut = (units: AvailableCut[], cutMeatId: number | null) =>
  units.filter((unit) => unit.cutMeatId === cutMeatId);

// Propone las N unidades mas antiguas del corte (FIFO)
export const proposeOldest = (units: AvailableCut[], cutMeatId: number | null, count: number) =>
  unitsOfCut(units, cutMeatId)
    .slice(0, Math.max(0, count))
    .map((unit) => unit.id);

// Kilos de las unidades elegidas, en centesimas.
export const selectedCents = (units: AvailableCut[], ids: number[]) =>
  units
    .filter((unit) => ids.includes(unit.id))
    .reduce((sum, unit) => sum + toCents(unit.kilograms), 0);

// Cortes que tienen unidades en la lista, con cuantas hay de cada uno
export const groupByCut = (units: AvailableCut[]) => {
  const groups = new Map<number, { id: number; name: string; count: number }>();

  for (const unit of units) {
    const group = groups.get(unit.cutMeatId);
    if (group) {
      group.count++;
    } else {
      groups.set(unit.cutMeatId, { id: unit.cutMeatId, name: unit.cutMeatName, count: 1 });
    }
  }

  return [...groups.values()];
};

// De la mas antigua a la mas nueva
const byOldest = (a: AvailableCut, b: AvailableCut) =>
  a.purchaseDate.localeCompare(b.purchaseDate) ||
  a.purchaseId - b.purchaseId ||
  a.pieceNumber - b.pieceNumber ||
  a.id - b.id;

// Las unidades libres mas las que ya tiene la venta (que no figuran como libres)
export const mergeUnits = (available: AvailableCut[], own: AvailableCut[]) => {
  const availableIds = new Set(available.map((unit) => unit.id));
  return [...available, ...own.filter((unit) => !availableIds.has(unit.id))].sort(byOldest);
};
