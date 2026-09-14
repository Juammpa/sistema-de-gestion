export interface Supplier {
  id: number;
  companyName: string;
  phoneNumber: string;
  currentAccountId: number;
  currentBalance: number;
}

export interface SupplierRequest {
  companyName: string;
  phoneNumber: string;
}
