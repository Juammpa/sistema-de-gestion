import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { ButcherShop, ButcherShopRequest } from '../interfaces/butcher-shop.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ButcherShopService {
  private http = inject(HttpClient);

  // Crea una sucursal para un cliente existente
  createButcherShop(butcherShop: ButcherShopRequest) {
    return this.http.post<ButcherShop>(`${environment.apiUrl}/butcher-shops`, butcherShop);
  }
}
