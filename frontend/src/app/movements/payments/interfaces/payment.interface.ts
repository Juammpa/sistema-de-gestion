export interface Payment {
  id: number;
  paymentType: string;
  date: Date;
  currentAccountId: number;
  amount: number;
  mirror: boolean;
  supplierId: number | null;
  supplierName: string | null;
}

export interface PaymentRequest {
  paymentType: string;
  amount: number;
  currentAccountId: number;
  date: string;
  supplierId?: number | null;
}
