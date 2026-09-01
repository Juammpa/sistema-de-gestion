export interface Supplier {
  id: number;
  companyName: string;
  phoneNumber: string;
}

export interface SupplierRequest {
  companyName: string;
  phoneNumber: string;
}
