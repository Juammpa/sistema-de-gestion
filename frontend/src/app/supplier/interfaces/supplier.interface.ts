export interface Supplier {
  id: number;
  companyName: string;
  phoneNumber: string;
  currentBalance: number;
}

export interface SupplierRequest {
  companyName: string;
  phoneNumber: string;
}
