import { ButcherShop } from '../../butcher-shop/interfaces/butcher-shop.interface';

// client.interface.ts
export interface Client {
  id: number;
  name: string;
  lastName: string;
  phoneNumber: string;
  butcherShops: ButcherShop[];
}

export interface ClientRequest {
  name: string;
  lastName: string;
  phoneNumber: string;
}
