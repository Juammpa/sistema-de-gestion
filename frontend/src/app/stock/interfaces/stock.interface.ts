import { SpecieValue } from '../../cut-meat/utils/specie.utils';
import { PurchaseTypeValue } from '../../movements/purchases/utils/purchase.utils';

// Una pieza de stock: una unidad de compra
export interface StockItem {
  purchaseId: number;
  unitNumber: number; // 1, 2, 3...
  totalUnits: number; // unidades de la compra (para mostrar "2 de 4")
  purchaseType: PurchaseTypeValue;
  specie: SpecieValue;
  kilograms: number; // kilos de la compra / unidades
  date: string; // yyyy-MM-dd
  supplierId: number;
  supplierName: string;
}

export interface SpecieTotals {
  kilograms: number;
  units: number;
}

export interface TypeTotals {
  purchaseType: PurchaseTypeValue;
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
