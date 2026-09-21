export interface CashPayment {
  id: number;
  date: string;
  paymentType: string;
  amount: number;
  clientName: string;
  butcherShopName: string;
  supplierName: string | null;
}

export interface CashWithdrawal {
  id: number;
  date: string;
  amount: number;
  supplierName: string;
}

export interface CashDay {
  date: string;
  payments: CashPayment[];
  withdrawals: CashWithdrawal[];
  total: number;
  cash: number;
  withdrawn: number;
  closingBalance: number;
}
