export interface Purchase {
  id: number;
  date: Date;
  purchaseTypeId: number;
  purchaseTypeName: string;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
  currentAccountId: number;
  subtotal: number;
}

export interface PurchaseRequest {
  purchaseTypeId: number;
  currentAccountId: number;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
  date?: string;
}
