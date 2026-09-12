export type MovementType = 'SALE' | 'PAYMENT';

interface BaseMovement {
  id: number;
  date: string;
  amount: number;
  balance: number;
}

export interface SaleMovement extends BaseMovement {
  type: 'SALE';
  cutMeatType: string;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
}

export interface PaymentMovement extends BaseMovement {
  type: 'PAYMENT';
  paymentType: string;
}

export type Movement = SaleMovement | PaymentMovement;
