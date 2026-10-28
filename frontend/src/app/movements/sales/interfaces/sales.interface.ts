import { AvailableCut } from '../../../stock/interfaces/stock.interface';

export interface Sale {
  id: number;
  clientId: number;
  date: Date;
  cutMeatId: number;
  cutMeatName: string;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
  subtotal: number;
  stockCuts?: AvailableCut[]; // solo viene al pedir una venta, no en los listados
}

export interface SaleRequest {
  clientId: number;
  butcherShopId: number;
  cutMeatId: number;
  stockCutIds: number[]; // unidades de stock que se venden
  date?: string;
}
