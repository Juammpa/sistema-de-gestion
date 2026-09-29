export type DashboardMovementKind = 'SALE' | 'COLLECTION' | 'PURCHASE' | 'PAYMENT';

export interface MonthlyFlow {
  year: number;
  month: number;
  sales: number;
  purchases: number;
  collections: number;
  payments: number;
}

export interface DebtRanking {
  id: number;
  name: string;
  balance: number;
}

export interface RecentMovement {
  id: number;
  date: string; // yyyy-MM-dd
  kind: DashboardMovementKind;
  counterparty: string;
  detail: string | null;
  amount: number;
}

export interface Dashboard {
  receivable: number;
  payable: number;
  previousReceivable: number;
  previousPayable: number;
  months: MonthlyFlow[]; // 12 meses, del mas viejo al actual
  topClients: DebtRanking[];
  topSuppliers: DebtRanking[];
  recentMovements: RecentMovement[];
}
