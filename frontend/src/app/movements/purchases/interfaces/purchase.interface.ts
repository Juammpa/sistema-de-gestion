export interface Purchase {
  id: number;
  date: Date;
  purchaseType: string;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
  currentAccountId: number;
  subtotal: number;
}

export interface PurchaseRequest {
  purchaseType: string;
  currentAccountId: number;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
  date?: string;
}
