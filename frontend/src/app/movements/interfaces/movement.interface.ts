export type MovementType = 'SALE' | 'PAYMENT' | 'PURCHASE';

interface BaseMovement {
  id: number;
  date: string;
  amount: number;
  balance: number;
}

export interface SaleMovement extends BaseMovement {
  type: 'SALE';
  cutMeatName: string;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
}

export interface PurchaseMovement extends BaseMovement {
  type: 'PURCHASE';
  purchaseTypeId: number;
  purchaseTypeName: string;
  unit: number;
  kilograms: number;
  pricePerKilo: number;
}

export interface PaymentMovement extends BaseMovement {
  type: 'PAYMENT';
  paymentType: string;
  customPaymentMethodName?: string | null; // Solo en pagos CUSTOM
}

// Un boton del filtro de tipo de movimiento (value null = todos)
export interface MovementFilterOption {
  value: MovementType | null;
  label: string;
}

export type Movement = SaleMovement | PaymentMovement | PurchaseMovement;
