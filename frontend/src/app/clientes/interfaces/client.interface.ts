import { ButcherShop } from '../../butcher-shop/interfaces/butcher-shop.interface';

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

export interface ClientCreateRequest extends ClientRequest {
  butcherShopName: string;
  butcherShopAddress: string;
}
