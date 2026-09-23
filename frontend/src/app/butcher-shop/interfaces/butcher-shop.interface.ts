export interface ButcherShop {
  id: number;
  name: string;
  address: string;
  clientId: number;
  currentAccountId: number;
  currentBalance: number;
}

export interface ButcherShopRequest {
  name: string;
  address: string;
  clientId: number;
}
