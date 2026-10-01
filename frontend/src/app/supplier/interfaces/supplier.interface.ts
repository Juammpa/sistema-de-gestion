export type SupplierType = 'BOVINE' | 'PORCINE' | 'BOTH';

export interface Supplier {
  id: number;
  companyName: string;
  phoneNumber: string;
  supplierType: SupplierType;
  currentAccountId: number;
  currentBalance: number;
}

export interface SupplierRequest {
  companyName: string;
  phoneNumber: string;
  supplierType: SupplierType;
}
