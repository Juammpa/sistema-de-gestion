import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import {
  ButcherShop,
  ButcherShopRequest,
  ButcherShopUpdateRequest,
} from '../interfaces/butcher-shop.interface';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ButcherShopService {
  private http = inject(HttpClient);

  // Crea una sucursal para un cliente existente
  createButcherShop(butcherShop: ButcherShopRequest) {
    return this.http.post<ButcherShop>(`${environment.apiUrl}/butcher-shops`, butcherShop);
  }

  // Edita los datos de una sucursal
  updateButcherShop(id: number, butcherShop: ButcherShopUpdateRequest) {
    return this.http.put<ButcherShop>(`${environment.apiUrl}/butcher-shops/${id}`, butcherShop);
  }

  // Elimina (logicamente) una sucursal: deja de mostrarse pero su historial se conserva.
  deleteButcherShop(id: number) {
    return this.http.delete<void>(`${environment.apiUrl}/butcher-shops/${id}`);
  }
}
