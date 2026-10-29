export interface CustomPaymentMethod {
  id: number;
  name: string;
  isActive: boolean;
}

export interface CustomPaymentMethodRequest {
  name: string;
}
