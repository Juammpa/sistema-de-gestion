export interface Payment {
  id: number;
  paymentType: string;
  date: Date;
  currentAccountId: number;
  amount: number;
  mirror: boolean;
  supplierId: number | null;
  supplierName: string | null;
  customPaymentMethodId?: number | null; // Solo en pagos CUSTOM
  customPaymentMethodName: string | null;
  rejected?: boolean; // Solo en cheques: ya tiene un rechazo
  rejectedPaymentId?: number | null; // Solo en rechazos: el cheque rechazado
  originClientName?: string | null;
  originButcherShopName?: string | null;
}

export interface PaymentRequest {
  paymentType: string;
  amount: number;
  currentAccountId: number;
  date?: string;
  supplierId?: number | null;
  customPaymentMethodId?: number | null;
}
