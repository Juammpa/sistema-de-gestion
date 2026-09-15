export interface Payment {
  id: number;
  paymentType: string;
  date: Date;
  currentAccountId: number;
  amount: number;
}

export interface PaymentRequest {
  paymentType: string;
  amount: number;
  currentAccountId: number;
  date: string;
}
