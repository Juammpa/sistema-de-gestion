import { SpecieValue } from '../../cut-meat/utils/specie.utils';

// Una pieza de stock: una unidad de compra
export interface StockItem {
  purchaseId: number;
  unitNumber: number; // 1, 2, 3...
  totalUnits: number; // unidades de la compra (para mostrar "2 de 4")
  purchaseTypeId: number;
  purchaseTypeName: string;
  specie: SpecieValue;
  kilograms: number; // kilos de la compra / unidades
  date: string; // yyyy-MM-dd
  supplierId: number;
  supplierName: string;
  editable: boolean; // la compra tiene mas de una unidad: se puede repartir el peso
  adjusted: boolean; // el peso fue ajustado a mano
  cuts: StockCut[]; // cortes sacados de esta pieza (vacio si no tiene)
}

export interface SpecieTotals {
  kilograms: number;
  units: number;
}

export interface TypeTotals {
  purchaseTypeId: number;
  purchaseTypeName: string;
  specie: SpecieValue;
  kilograms: number;
  units: number;
}

// Totales del periodo completo (no cambian con los filtros de la pantalla)
export interface StockSummary {
  totalKilograms: number;
  totalUnits: number;
  bovine: SpecieTotals;
  porcine: SpecieTotals;
  byType: TypeTotals[]; // solo los tipos que tuvieron compra en el periodo.
}

export interface StockResponse {
  summary: StockSummary;
  items: StockItem[];
}

// Una unidad de corte sacada de una pieza
export interface StockCut {
  id: number;
  cutMeatId: number;
  cutMeatName: string;
  kilograms: number;
  sold: boolean; // si ya se vendió, no se puede editar
}

// Un corte tal como se envia al guardar los cortes de una pieza
export interface StockCutRequest {
  id?: number; // sin id = unidad nueva
  cutMeatId: number;
  kilograms: number;
}

// Una unidad de corte sin vender: se puede elegir al armar una venta
export interface AvailableCut {
  id: number;
  cutMeatId: number;
  cutMeatName: string;
  kilograms: number;
  purchaseId: number;
  pieceNumber: number;
  purchaseUnits: number; // unidades de la compra (para mostrar "pieza 2 de 4")
  purchaseDate: string; // yyyy-MM-dd
  purchaseTypeName: string;
  supplierName: string;
}
