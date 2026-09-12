export interface Payment {
  id: number;
  paymentType: string;
  date: Date;
  clientId: number;
  amount: number;
}

export interface PaymentRequest {
  paymentType: string;
  amount: number;
  butcherShopId: number;
  clientId: number;
}
