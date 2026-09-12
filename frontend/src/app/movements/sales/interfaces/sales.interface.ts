export interface Sale {
  id: number;
  clientId: number;
  date: Date;
  cutMeatType: string;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
  subtotal: number;
}

export interface SaleRequest {
  clientId: number;
  butcherShopId: number;
  cutMeatId: number;
  kilograms: number;
  unit: number;
}
